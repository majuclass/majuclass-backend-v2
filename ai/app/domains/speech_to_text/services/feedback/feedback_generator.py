from typing import List, Optional

from app.domains.speech_to_text.models.evaluation_models import Scores


class FeedbackGenerator:
    def __init__(self):
        """초기화"""
        print("[FeedbackGenerator] 초기화 완료")

    def generate(
        self,
        scores: Scores,
        is_correct: bool,
        stt_keywords: List[str],
        answer_keywords: List[str],
        metadata: Optional[dict] = None
    ) -> str:
        print("\n[FeedbackGenerator] 피드백 생성 중...")

        if is_correct:
            feedback = self._generate_positive_feedback(scores, stt_keywords, answer_keywords)
        else:
            feedback = self._generate_improvement_feedback(scores, stt_keywords, answer_keywords)

        print(f"  ✓ 피드백: {feedback}")
        return feedback

    def _generate_positive_feedback(
        self,
        scores: Scores,
        stt_keywords: List[str],
        answer_keywords: List[str]
    ) -> str:
        # 완벽한 경우 (모든 점수 0.9 이상)
        if scores.semantic >= 0.9 and scores.keyword >= 0.9 and scores.phonetic >= 0.9:
            return "정답입니다! 완벽해요! 👍"

        # 발음이 약간 부족한 경우
        if scores.phonetic < 0.85:
            return "정답입니다! 하지만 발음을 좀 더 정확하게 해보세요."

        # 키워드가 약간 부족한 경우
        if scores.keyword < 0.9:
            missing = self._find_missing_keywords(stt_keywords, answer_keywords)
            if missing:
                missing_str = "', '".join(missing[:2])  # 최대 2개만
                return f"정답입니다! 하지만 '{missing_str}' 단어를 넣으면 더 좋아요."

        # 의미가 약간 부족한 경우
        if scores.semantic < 0.85:
            return "정답입니다! 하지만 좀 더 정확하게 말해보세요."

        # 기본 긍정 피드백
        return "정답입니다! 잘했어요!"

    def _generate_improvement_feedback(
        self,
        scores: Scores,
        stt_keywords: List[str],
        answer_keywords: List[str]
    ) -> str:
        """
        오답일 때 피드백 생성

        가장 약한 부분을 찾아서 개선점 제시
        """
        # 점수 분석: 가장 낮은 점수 찾기
        score_dict = {
            "semantic": scores.semantic,
            "keyword": scores.keyword,
            "phonetic": scores.phonetic
        }
        weakest = min(score_dict, key=score_dict.get)
        weakest_score = score_dict[weakest]

        # 매우 낮은 경우 (0.3 미만)
        if scores.weighted < 0.3:
            return "다시 한번 생각해보세요."

        # 키워드가 가장 약한 경우
        if weakest == "keyword" and scores.keyword < 0.5:
            missing = self._find_missing_keywords(stt_keywords, answer_keywords)
            if missing:
                missing_str = "', '".join(missing[:2])
                return f"조금 아쉬워요. '{missing_str}' 단어를 넣어보세요."
            return "중요한 단어를 더 넣어보세요."

        # 발음이 가장 약한 경우
        if weakest == "phonetic" and scores.phonetic < 0.5:
            return "의미는 맞지만, 발음을 좀 더 정확하게 해보세요."

        # 의미가 가장 약한 경우
        if weakest == "semantic" and scores.semantic < 0.5:
            return "조금 더 정확하게 말해보세요."

        # 거의 맞았지만 아쉬운 경우 (0.6 이상)
        if scores.weighted >= 0.6:
            return "아쉬워요! 조금만 더 노력하면 정답이에요."

        # 조금 부족한 경우 (0.5 이상)
        if scores.weighted >= 0.5:
            return "좀 더 생각해보세요."

        # 많이 부족한 경우
        return "다시 한번 시도해보세요."

    def _find_missing_keywords(
        self,
        stt_keywords: List[str],
        answer_keywords: List[str]
    ) -> List[str]:
        """
        누락된 키워드 찾기

        Args:
            stt_keywords: STT 키워드
            answer_keywords: 정답 키워드 (동의어 포함)

        Returns:
            누락된 키워드 리스트
        """
        # 동의어 그룹을 고려하지 않고 단순 비교
        # (실제로는 동의어가 확장되어 들어오므로 문제없음)
        stt_set = set(stt_keywords)
        answer_set = set(answer_keywords)

        missing = answer_set - stt_set

        # 원본 키워드만 반환 (동의어는 제외)
        # 간단한 휴리스틱: 2글자 이상, 한글만
        filtered = [
            kw for kw in missing
            if len(kw) >= 2 and all('\uac00' <= c <= '\ud7a3' or c.isspace() for c in kw)
        ]

        return filtered[:3]  # 최대 3개만

    def generate_detailed_feedback(
        self,
        scores: Scores,
        is_correct: bool,
        stt_keywords: List[str],
        answer_keywords: List[str],
        stt_text: str,
        answer_text: str
    ) -> dict:
        """
        상세 피드백 생성 (디버그용)

        Args:
            scores: 점수
            is_correct: 정답 여부
            stt_keywords: STT 키워드
            answer_keywords: 정답 키워드
            stt_text: STT 텍스트
            answer_text: 정답 텍스트

        Returns:
            상세 피드백 딕셔너리
        """
        feedback_text = self.generate(
            scores, is_correct, stt_keywords, answer_keywords
        )

        return {
            "feedback": feedback_text,
            "is_correct": is_correct,
            "scores": {
                "semantic": scores.semantic,
                "keyword": scores.keyword,
                "phonetic": scores.phonetic,
                "weighted": scores.weighted
            },
            "analysis": {
                "stt_keywords": stt_keywords,
                "answer_keywords": answer_keywords[:10],  # 최대 10개
                "missing_keywords": self._find_missing_keywords(stt_keywords, answer_keywords),
                "weakest_dimension": self._get_weakest_dimension(scores)
            },
            "texts": {
                "stt": stt_text,
                "answer": answer_text
            }
        }

    def _get_weakest_dimension(self, scores: Scores) -> str:
        """
        가장 약한 차원 찾기

        Args:
            scores: 점수

        Returns:
            "semantic", "keyword", "phonetic" 중 하나
        """
        score_dict = {
            "semantic": scores.semantic,
            "keyword": scores.keyword,
            "phonetic": scores.phonetic
        }
        return min(score_dict, key=score_dict.get)


# 편의 함수
def generate_feedback(
    scores: Scores,
    is_correct: bool,
    stt_keywords: List[str],
    answer_keywords: List[str]
) -> str:
    """
    간단한 피드백 생성 함수

    Args:
        scores: 점수
        is_correct: 정답 여부
        stt_keywords: STT 키워드
        answer_keywords: 정답 키워드

    Returns:
        피드백 문자열
    """
    generator = FeedbackGenerator()
    return generator.generate(scores, is_correct, stt_keywords, answer_keywords)
