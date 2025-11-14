"""
EvaluationPipeline: 평가 파이프라인

전처리 → 스코어링 → 판정 → 피드백의 전체 평가 프로세스를 조율합니다.
"""

from typing import Optional, Dict, Any

from app.domains.speech_to_text.models.evaluation_models import (
    Evaluation,
    PreprocessingResult,
    Scores
)
from app.domains.speech_to_text.ports.cache_port import CachePort
from app.domains.speech_to_text.services.feedback.feedback_generator import FeedbackGenerator
from app.domains.speech_to_text.services.preprocessing.keyword_utils import (
    extract_keywords,
    expand_synonyms
)
from app.domains.speech_to_text.services.preprocessing.normalizer import Normalizer
from app.domains.speech_to_text.services.scoring.multi_dimension_scorer import MultiDimensionScorer


def _judge(scores: Scores) -> bool:

    print("\n[3/4] 판정 중...")

    # 단일 임계값 정책 (과거 학년별 정책 제거)
    threshold = 0.6

    is_correct = scores.weighted >= threshold

    print(f"  - 가중 점수: {scores.weighted:.4f}")
    print(f"  - 임계값: {threshold:.2f}")
    print(f"  - 판정: {'정답' if is_correct else '오답'}")

    return is_correct


def _build_debug_info(
        preprocessing: PreprocessingResult,
    scores: Scores,
    metadata: Dict[str, Any]
) -> dict:
    return {
        "preprocessing": {
            "stt_keywords": preprocessing.stt_keywords,
            "answer_keywords": preprocessing.answer_keywords[:10],
            "normalized_stt": preprocessing.normalized_stt,
            "normalized_answer": preprocessing.normalized_answer
        },
        "scores": scores.to_dict(),
        "metadata": metadata
    }


class EvaluationPipeline:
    def __init__(
        self,
        normalizer: Optional[Normalizer] = None,
        scorer: Optional[MultiDimensionScorer] = None,
        feedback_generator: Optional[FeedbackGenerator] = None,
        cache_port: Optional[CachePort] = None
    ):
        self.normalizer = normalizer or Normalizer()
        self.scorer = scorer or MultiDimensionScorer(cache_port=cache_port)
        self.feedback_generator = feedback_generator or FeedbackGenerator()
        self.cache = cache_port

        print("[EvaluationPipeline] 초기화 완료")

    async def execute(
        self,
        stt_text: str,
        answer_text: str,
        metadata: Optional[Dict[str, Any]] = None
    ) -> Evaluation:
        print("\n" + "=" * 60)
        print("[EvaluationPipeline] 평가 시작")
        print("=" * 60)
        print(f"STT: {stt_text}")
        print(f"정답: {answer_text}")

        metadata = metadata or {}

        # 1. 전처리
        preprocessing = await self._preprocess(stt_text, answer_text)

        # 2. 스코어링
        scores = await self._calculate_scores(preprocessing, metadata)

        # 3. 판정
        is_correct = _judge(scores)

        # 4. 피드백 생성
        feedback = self._generate_feedback(
            scores,
            is_correct,
            preprocessing
        )

        # 5. 디버그 정보
        debug_info = _build_debug_info(preprocessing, scores, metadata)

        print(f"\n최종 판정: {'✅ 정답' if is_correct else '❌ 오답'}")
        print(f"피드백: {feedback}")
        print("=" * 60 + "\n")

        return Evaluation(
            normalized_stt=preprocessing.normalized_stt,
            normalized_answer=preprocessing.normalized_answer,
            scores=scores,
            is_correct=is_correct,
            feedback=feedback,
            debug_info=debug_info
        )

    async def _preprocess(
        self, stt_text: str, answer_text: str
    ) -> PreprocessingResult:
        print("\n[1/4] 전처리 중...")

        # 정규화
        normalized_stt = self.normalizer.normalize(stt_text)
        normalized_answer = self.normalizer.normalize(answer_text)

        print(f"  - STT 정규화: {normalized_stt}")
        print(f"  - 정답 정규화: {normalized_answer}")

        # STT 키워드 추출
        stt_keywords = extract_keywords(normalized_stt)

        # 정답 키워드 추출 + 캐싱
        answer_keywords = await self._get_cached_keywords(normalized_answer)

        print(f"  - STT 키워드: {stt_keywords}")
        print(f"  - 정답 키워드: {answer_keywords[:10]}")  # 최대 10개만 표시

        return PreprocessingResult(
            original_stt=stt_text,
            original_answer=answer_text,
            normalized_stt=normalized_stt,
            normalized_answer=normalized_answer,
            stt_keywords=stt_keywords,
            answer_keywords=answer_keywords
        )

    async def _get_cached_keywords(self, answer_text: str) -> list:
        if not self.cache:
            # 캐시 없으면 바로 추출
            keywords = extract_keywords(answer_text)
            return expand_synonyms(keywords)

        # 캐시 키 생성
        cache_key = f"keywords:{hash(answer_text)}"

        # 캐시 확인
        cached = await self.cache.get(cache_key)
        if cached:
            print(f"  ✓ 키워드 캐시 히트")
            return cached

        # 캐시 미스: 추출 후 저장
        print(f"  ✓ 키워드 캐시 미스")
        keywords = extract_keywords(answer_text)
        expanded = expand_synonyms(keywords)

        await self.cache.set(cache_key, expanded, ttl=3600)

        return expanded

    async def _calculate_scores(
        self,
        preprocessing: PreprocessingResult,
        metadata: Dict[str, Any]
    ) -> Scores:
        print("\n[2/4] 점수 계산 중...")

        scores = await self.scorer.score(
            stt_text=preprocessing.normalized_stt,
            answer_text=preprocessing.normalized_answer,
            stt_keywords=preprocessing.stt_keywords,
            answer_keywords=preprocessing.answer_keywords,
            metadata=metadata
        )

        return scores

    def _generate_feedback(
        self,
        scores: Scores,
        is_correct: bool,
        preprocessing: PreprocessingResult
    ) -> str:
        print("\n[4/4] 피드백 생성 중...")

        feedback = self.feedback_generator.generate(
            scores=scores,
            is_correct=is_correct,
            stt_keywords=preprocessing.stt_keywords,
            answer_keywords=preprocessing.answer_keywords
        )

        return feedback


async def quick_evaluate(
    stt_text: str,
    answer_text: str,
    metadata: Optional[Dict[str, Any]] = None
) -> Evaluation:

    pipeline = EvaluationPipeline()
    return await pipeline.execute(
        stt_text,
        answer_text,
        metadata=metadata or {}
    )
