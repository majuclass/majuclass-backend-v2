"""
MultiDimensionScorer: 다차원 점수 계산

3가지 차원의 점수를 계산하고 조합합니다:
1. Semantic (의미): 문장의 의미적 유사도
2. Keyword (키워드): 필수 키워드 포함 여부
3. Phonetic (발음): 발음의 정확도
"""

from typing import List, Optional, Dict, Any

from app.domains.speech_to_text.models.evaluation_models import Scores
from app.domains.speech_to_text.ports.cache_port import CachePort
from app.domains.speech_to_text.services.preprocessing.keyword_utils import (
    calculate_keyword_coverage
)
from app.domains.speech_to_text.services.scoring.phonetic_utils import (
    phonetic_similarity,
    syllable_similarity
)
from app.domains.speech_to_text.services.scoring.semantic_similarity import SemanticSimilarity


def _calculate_keyword(
        stt_keywords: List[str], answer_keywords: List[str]
) -> float:
    print("\n[2/3] 키워드 매칭 계산 중...")

    if not answer_keywords:
        # 정답 키워드가 없으면 100% (비교 불가)
        print("  ⚠ 정답 키워드 없음 → 1.0")
        return 1.0

    # 키워드 커버리지 계산
    coverage = calculate_keyword_coverage(stt_keywords, answer_keywords)

    # 매칭된 키워드 출력
    matched = set(stt_keywords) & set(answer_keywords)
    missing = set(answer_keywords) - set(stt_keywords)

    print(f"  - STT 키워드: {stt_keywords}")
    print(f"  - 정답 키워드: {answer_keywords}")
    print(f"  ✓ 매칭: {list(matched)}")
    if missing:
        print(f"  ✗ 누락: {list(missing)[:5]}")  # 최대 5개만 표시
    print(f"  ✓ 키워드 점수: {coverage:.4f}")

    return coverage


def _calculate_phonetic(stt_text: str, answer_text: str) -> float:
    print("\n[3/3] 발음 정확도 계산 중...")

    try:
        # 자모 기반 유사도
        jamo_sim = phonetic_similarity(stt_text, answer_text)

        # 음절 기반 유사도
        syllable_sim = syllable_similarity(stt_text, answer_text)

        # 평균 (자모에 더 높은 가중치)
        phonetic_score = jamo_sim * 0.7 + syllable_sim * 0.3

        print(f"  - 자모 유사도: {jamo_sim:.4f}")
        print(f"  - 음절 유사도: {syllable_sim:.4f}")
        print(f"  ✓ 발음 점수: {phonetic_score:.4f}")

        return phonetic_score

    except Exception as e:
        print(f"  ✗ 발음 계산 실패: {str(e)}")
        # 에러 시 폴백: 자모만 사용
        return phonetic_similarity(stt_text, answer_text)


class MultiDimensionScorer:

    def __init__(
        self,
        semantic_similarity: Optional[SemanticSimilarity] = None,
        cache_port: Optional[CachePort] = None,
        weights: Optional[Dict[str, float]] = None
    ):
        # Semantic Similarity 초기화
        if semantic_similarity:
            self.semantic = semantic_similarity
        else:
            self.semantic = SemanticSimilarity(cache_port=cache_port)

        self.cache_port = cache_port

        # 기본 가중치
        self.weights = weights or {
            "semantic": 0.5,   # 의미 유사도: 50%
            "keyword": 0.3,    # 키워드 매칭: 30%
            "phonetic": 0.2    # 발음 정확도: 20%
        }

        print(f"[MultiDimensionScorer] 초기화 완료")
        print(f"  - 가중치: {self.weights}")

    async def score(
        self,
        stt_text: str,
        answer_text: str,
        stt_keywords: List[str],
        answer_keywords: List[str],
        metadata: Optional[Dict[str, Any]] = None
    ) -> Scores:
        print("\n" + "=" * 60)
        print("[MultiDimensionScorer] 점수 계산 시작")
        print("=" * 60)

        # 1. Semantic Similarity (의미 유사도)
        semantic_score = await self._calculate_semantic(stt_text, answer_text)

        # 2. Keyword Matching (키워드 매칭)
        keyword_score = _calculate_keyword(stt_keywords, answer_keywords)

        # 3. Phonetic Accuracy (발음 정확도)
        phonetic_score = _calculate_phonetic(stt_text, answer_text)

        # 4. Weighted Average (가중 평균)
        weighted_score = self._calculate_weighted(
            semantic_score, keyword_score, phonetic_score
        )

        print("\n[MultiDimensionScorer] 점수 계산 완료:")
        print(f"  - Semantic:  {semantic_score:.4f} (가중치: {self.weights['semantic']})")
        print(f"  - Keyword:   {keyword_score:.4f} (가중치: {self.weights['keyword']})")
        print(f"  - Phonetic:  {phonetic_score:.4f} (가중치: {self.weights['phonetic']})")
        print(f"  - Weighted:  {weighted_score:.4f}")
        print("=" * 60 + "\n")

        return Scores(
            semantic=semantic_score,
            keyword=keyword_score,
            phonetic=phonetic_score,
            weighted=weighted_score
        )

    async def _calculate_semantic(self, stt_text: str, answer_text: str) -> float:
        print("\n[1/3] 의미 유사도 계산 중...")

        try:
            # 비동기 계산 (캐싱 지원)
            similarity = await self.semantic.calculate_similarity_async(
                stt_text,
                answer_text,
                use_cache=True
            )

            print(f"  ✓ 의미 유사도: {similarity:.4f}")
            return similarity

        except Exception as e:
            print(f"  ✗ 의미 유사도 계산 실패: {str(e)}")
            # 에러 시 폴백: 동기 방식
            return self.semantic.calculate_similarity(
                stt_text,
                answer_text,
                use_cache=False
            )

    def _calculate_weighted(
        self, semantic: float, keyword: float, phonetic: float
    ) -> float:
        weighted = (
            semantic * self.weights["semantic"] +
            keyword * self.weights["keyword"] +
            phonetic * self.weights["phonetic"]
        )

        return round(weighted, 4)

    def set_weights(self, weights: Dict[str, float]) -> None:
        total = sum(weights.values())
        if not (0.99 <= total <= 1.01):  # 부동소수점 오차 허용
            raise ValueError(f"가중치 합은 1.0이어야 합니다 (현재: {total})")

        self.weights = weights
        print(f"[MultiDimensionScorer] 가중치 변경: {weights}")

    def get_weights(self) -> Dict[str, float]:
        return self.weights.copy()


async def quick_score(
    stt_text: str,
    answer_text: str,
    stt_keywords: List[str],
    answer_keywords: List[str]
) -> Scores:
    scorer = MultiDimensionScorer()
    return await scorer.score(
        stt_text, answer_text, stt_keywords, answer_keywords
    )
