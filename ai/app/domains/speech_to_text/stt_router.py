from fastapi import APIRouter, HTTPException, status, Request, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.domains.speech_to_text.models.stt_request_models import SpeechToTextRequestModel
from app.domains.speech_to_text.models.stt_response_models import SpeechToTextResponse
from app.domains.speech_to_text.services.speech_analysis_service import SpeechAnalysisService
from app.domains.speech_to_text.repositories.session_repository import SessionRepository
from app.domains.speech_to_text.repositories.session_stt_answer_repository import SessionSTTAnswerRepository
from app.domains.speech_to_text.utils.presigned_url_service import PresignedUrlService
from app.common.api_response import ApiResponse
from app.common.exceptions import (
    SessionNotFoundError,
    PermissionDeniedError,
    SequenceNotFoundError
)
from app.database import get_session

router = APIRouter(
    prefix="/ai",
    tags=["STT"],
)
speech_analysis_service = SpeechAnalysisService()
presigned_url_service = PresignedUrlService()

# 유사도 임계값
SIMILARITY_THRESHOLD = 0.7

@router.post(
    "/stt-analyze/{session_id}/{seq_no}",
    response_model=ApiResponse[SpeechToTextResponse],
    summary="음성 텍스트 변환 후 정답 판별 및 DB 저장",
    description="S3의 오디오 파일을 STT로 변환하고 정답과 비교하여 session_stt_answers 테이블에 저장합니다.",
    status_code=status.HTTP_200_OK
)
async def stt_analyze(
    session_id: int,
    seq_no: int,
    body: SpeechToTextRequestModel,
    req: Request,
    db: AsyncSession = Depends(get_session)
) -> ApiResponse[SpeechToTextResponse]:
    try:
        print("\n" + "=" * 60)
        print("[API] STT 분석 요청 수신")
        print("=" * 60)
        print(f"Session ID: {session_id}")
        print(f"Seq No: {seq_no}")
        print(f"Audio S3 Key: {body.audio_s3_key}")

        # ===== 1단계: 인증 - middleware에서 저장한 user_id 가져오기 =====
        user_id = req.state.user_id
        print(f"User ID: {user_id}")

        # ===== 2단계: Repository 초기화 =====
        session_repo = SessionRepository(db)
        answer_repo = SessionSTTAnswerRepository(db)

        # ===== 3단계: 세션 검증 =====
        try:
            session = await session_repo.verify_session(session_id, user_id)
            scenario_id = session.scenario_id
            print(f"세션 검증 완료: scenario_id={scenario_id}")
        except (SessionNotFoundError, PermissionDeniedError) as e:
            print(f"❌ 세션 검증 실패: {e.message}")
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=e.message
            )

        # ===== 4단계: seq_id 및 정답 조회 =====
        try:
            seq_id = await session_repo.get_seq_id(scenario_id, seq_no)
            answer_text = await session_repo.get_answer_text(seq_id)
            print(f"seq_id={seq_id}, 정답={answer_text}")
        except SequenceNotFoundError as e:
            print(f"❌ 시퀀스 조회 실패: {e.message}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=e.message
            )

        # ===== 5단계: Presigned URL 발급 =====
        try:
            presigned_url = presigned_url_service.get_presigned_url(
                file_name=body.audio_s3_key,
                operation="getObject"
            )
            print(f"Presigned URL 발급 완료: {presigned_url[:50]}...")
        except Exception as e:
            print(f"❌ Presigned URL 발급 실패: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Presigned URL 발급 실패: {str(e)}"
            )

        # ===== 6단계: 음성 분석 (STT + 유사도) =====
        analysis_result = speech_analysis_service.analyze_speech(
            presigned_url=presigned_url,
            answer=answer_text,
            language="ko"  # 한국어 고정
        )

        transcribed_text = analysis_result["transcribed_text"]
        similarity_score = analysis_result["similarity_score"]
        is_correct = analysis_result["is_correct"]

        print(f"STT 결과: {transcribed_text}")
        print(f"유사도: {similarity_score}, 정답 여부: {is_correct}")

        # ===== 7단계: DB 저장 =====
        saved_answer = await answer_repo.save_answer(
            session_id=session_id,
            seq_id=seq_id,
            transcribed_text=transcribed_text,
            answer_text=answer_text,
            similarity_score=float(similarity_score),
            is_correct=is_correct,
            audio_s3_key=body.audio_s3_key
        )

        print(f"DB 저장 완료: answer_id={saved_answer.id}, attempt_no={saved_answer.attempt_no}")

        # ===== 8단계: 응답 데이터 생성 =====
        response_data = SpeechToTextResponse(
            session_stt_answer_id=saved_answer.id,
            transcribed_text=transcribed_text,
            answer_text=answer_text,
            similarity_score=float(similarity_score),
            is_correct=is_correct,
            attempt_no=saved_answer.attempt_no
        )

        print("\n" + "=" * 60)
        print("[API] 응답 전송 완료")
        print("=" * 60)

        return ApiResponse.success(message="음성 분석이 완료되었습니다.", data=response_data)

    except HTTPException:
        # 이미 처리된 HTTP 예외는 그대로 전달
        raise

    except ValueError as e:
        # 잘못된 요청 데이터
        print(f"\n❌ [API] 잘못된 요청: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 요청입니다: {str(e)}"
        )

    except Exception as e:
        # 서버 내부 오류
        print(f"\n❌ [API] 서버 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"음성 분석 중 오류가 발생했습니다: {str(e)}"
        )