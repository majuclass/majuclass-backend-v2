import asyncio
from typing import Optional

from sqlalchemy.ext.asyncio import AsyncSession

from app.domains.speech_to_text.models.evaluation_models import (
    EvaluationRequest,
    EvaluationContext,
    EvaluationResult,
    AudioNotFoundError,
    STTTimeoutError
)
from app.domains.speech_to_text.ports.cache_port import CachePort
from app.domains.speech_to_text.ports.repository_port import RepositoryPort
from app.domains.speech_to_text.ports.storage_port import StoragePort
from app.domains.speech_to_text.ports.stt_port import STTPort
from app.domains.speech_to_text.services.evaluation_pipeline import EvaluationPipeline


class EvaluatorFacade:
    def __init__(
        self,
        storage_port: StoragePort,
        stt_port: STTPort,
        repository_port: RepositoryPort,
        pipeline: EvaluationPipeline,
        cache_port: Optional[CachePort] = None
    ):
        self.storage = storage_port
        self.stt = stt_port
        self.repository = repository_port
        self.pipeline = pipeline
        self.cache = cache_port

        print("[EvaluatorFacade] 초기화 완료")

    async def evaluate(
        self,
        request: EvaluationRequest,
        db: AsyncSession
    ) -> EvaluationResult:
        print("\n" + "=" * 80)
        print("[EvaluatorFacade] 평가 요청 수신")
        print("=" * 80)
        print(f"사용자 ID: {request.user_id}")
        print(f"세션 ID: {request.session_id}")
        print(f"시퀀스 번호: {request.seq_no}")
        print(f"오디오 키: {request.audio_s3_key}")

        try:
            # 1. 사전 검증 & 데이터 준비
            context = await self._prepare_context(request, db)

            # 2. 오디오 다운로드 & STT
            audio_data = await self._get_audio(context.audio_s3_key)
            stt_result = await self._transcribe(audio_data, context.language)

            # 3. 평가 파이프라인 실행
            evaluation = await self.pipeline.execute(
                stt_text=stt_result.text,
                answer_text=context.answer_text,
                metadata=context.metadata
            )

            # 4. DB 저장
            result = await self._persist(context, stt_result, evaluation, db)

            print("\n" + "=" * 80)
            print("[EvaluatorFacade] 평가 완료")
            print("=" * 80)

            return result

        except AudioNotFoundError as e:
            print(f"\n⚠️  오디오 파일 없음: {str(e)}")
            return await self._handle_silence(request, db, e)

        except STTTimeoutError as e:
            print(f"\n⚠️  STT 타임아웃: {str(e)}")
            return await self._handle_timeout(request, db, e)

    async def _prepare_context(
        self,
        request: EvaluationRequest,
        db: AsyncSession
    ) -> EvaluationContext:
        print("\n[1/4] 컨텍스트 준비 중...")

        # 세션 검증
        session = await self.repository.verify_session(
            request.session_id,
            request.user_id,
            db
        )
        print(f"  ✓ 세션 검증 완료: scenario_id={session.scenario_id}")

        # seq_id 조회
        seq_id = await self.repository.get_seq_id(
            session.scenario_id,
            request.seq_no,
            db
        )
        print(f"  ✓ seq_id 조회: {seq_id}")

        # 정답 조회
        answer_text = await self.repository.get_answer_text(seq_id, db)
        print(f"  ✓ 정답 조회: {answer_text}")

        return EvaluationContext(
            session_id=request.session_id,
            seq_id=seq_id,
            audio_s3_key=request.audio_s3_key,
            answer_text=answer_text,
            language=request.language,
            metadata={
                "student_id": request.user_id,
                "scenario_id": session.scenario_id
            }
        )

    async def _get_audio(self, s3_key: str) -> bytes:
        print("\n[2/4] 오디오 다운로드 중...")

        try:
            audio_data = await self.storage.download(s3_key)
            print(f"  ✓ 다운로드 완료: {len(audio_data)} bytes")
            return audio_data
        except FileNotFoundError:
            raise AudioNotFoundError(f"오디오 파일 없음: {s3_key}")
        except Exception as e:
            raise AudioNotFoundError(f"오디오 다운로드 실패: {str(e)}")

    async def _transcribe(self, audio_data: bytes, language: str) -> any:
        print("\n[3/4] STT 변환 중...")

        try:
            # 30초 타임아웃
            stt_result = await asyncio.wait_for(
                self.stt.transcribe(audio_data, language),
                timeout=30.0
            )

            print(f"  ✓ STT 완료: {stt_result.text}")
            return stt_result

        except asyncio.TimeoutError:
            raise STTTimeoutError("STT 처리 시간 초과 (30초)")

    async def _persist(
        self,
        context: EvaluationContext,
        stt_result: any,
        evaluation: any,
        db: AsyncSession
    ) -> EvaluationResult:

        print("\n[4/4] DB 저장 중...")

        saved = await self.repository.save_evaluation(
            session_id=context.session_id,
            seq_id=context.seq_id,
            audio_s3_key=context.audio_s3_key,
            transcribed_text=stt_result.text,
            answer_text=context.answer_text,
            weighted_score=evaluation.scores.weighted,
            is_correct=evaluation.is_correct,
            feedback_text=evaluation.feedback,
            db=db
        )

        print(f"  ✓ 저장 완료: answer_id={saved.id}, attempt_no={saved.attempt_no}")

        return EvaluationResult(
            answer_id=saved.id,
            session_id=context.session_id,
            seq_id=context.seq_id,
            transcribed_text=stt_result.text,
            answer_text=context.answer_text,
            scores=evaluation.scores,
            is_correct=evaluation.is_correct,
            feedback=evaluation.feedback,
            attempt_no=saved.attempt_no
        )

    async def _handle_silence(
        self,
        request: EvaluationRequest,
        db: AsyncSession,
    ) -> EvaluationResult:
        print("\n[침묵 처리] 오디오 파일 없음")

        # 컨텍스트 준비 (세션 검증은 필요)
        context = await self._prepare_context(request, db)

        # 침묵 처리 저장
        saved = await self.repository.save_evaluation(
            session_id=context.session_id,
            seq_id=context.seq_id,
            audio_s3_key=request.audio_s3_key,
            transcribed_text="",
            answer_text=context.answer_text,
            weighted_score=0.0,
            is_correct=False,
            feedback_text="음성이 감지되지 않았습니다. 다시 시도해주세요.",
            db=db
        )

        from app.domains.speech_to_text.models.evaluation_models import Scores

        return EvaluationResult(
            answer_id=saved.id,
            session_id=context.session_id,
            seq_id=context.seq_id,
            transcribed_text="",
            answer_text=context.answer_text,
            scores=Scores(semantic=0.0, keyword=0.0, phonetic=0.0, weighted=0.0),
            is_correct=False,
            feedback=saved.feedback_text,
            attempt_no=saved.attempt_no
        )

    async def _handle_timeout(
        self,
        request: EvaluationRequest,
        db: AsyncSession,
    ) -> EvaluationResult:

        print("\n[타임아웃 처리] STT 타임아웃")

        context = await self._prepare_context(request, db)

        saved = await self.repository.save_evaluation(
            session_id=context.session_id,
            seq_id=context.seq_id,
            audio_s3_key=request.audio_s3_key,
            transcribed_text="",
            answer_text=context.answer_text,
            weighted_score=0.0,
            is_correct=False,
            feedback_text="음성 인식에 시간이 너무 오래 걸렸습니다. 다시 시도해주세요.",
            db=db
        )

        from app.domains.speech_to_text.models.evaluation_models import Scores

        return EvaluationResult(
            answer_id=saved.id,
            session_id=context.session_id,
            seq_id=context.seq_id,
            transcribed_text="",
            answer_text=context.answer_text,
            scores=Scores(semantic=0.0, keyword=0.0, phonetic=0.0, weighted=0.0),
            is_correct=False,
            feedback=saved.feedback_text,
            attempt_no=saved.attempt_no
        )
