from typing import Tuple

from sentence_transformers import SentenceTransformer, util


class SimilarityService:
    # def __init__(self, model_name: str = "jhgan/ko-sroberta-multitask"):
    def __init__(self, model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS"):

        print(f"[SimilarityService] 모델 로딩 중: {model_name}")
        self.model = SentenceTransformer(model_name)
        self.similarity_threshold = 0.7  # 70% 이상 유사하면 정답
        print(f"[SimilarityService] 모델 로딩 완료")

    def calculate_similarity(self, text1: str, text2: str) -> float:

        # 1단계: 문장을 벡터로 변환 (임베딩)
        # encode() 메서드가 문장 → 벡터 변환을 수행
        # convert_to_tensor=True: PyTorch 텐서로 변환 (계산 최적화)
        embedding1 = self.model.encode(text1, convert_to_tensor=True)
        embedding2 = self.model.encode(text2, convert_to_tensor=True)

        # 2단계: 코사인 유사도 계산
        # util.cos_sim(): Sentence Transformers가 제공하는 유틸리티 함수
        # 두 벡터 간의 코사인 유사도를 계산
        cosine_score = util.cos_sim(embedding1, embedding2)

        # 3단계: 텐서에서 실수 값으로 변환
        # cosine_score는 2차원 텐서 [[0.85]] 형태
        # .item()으로 단일 숫자 0.85 추출
        similarity_score = cosine_score.item()

        print(f"[SimilarityService] 유사도: {similarity_score:.4f}")
        print(f"  - 문장1: {text1}")
        print(f"  - 문장2: {text2}")

        return similarity_score

    def is_similar(self, text1: str, text2: str) -> Tuple[bool, float]:

        # 유사도 계산
        similarity_score = self.calculate_similarity(text1, text2)

        # 임계값과 비교하여 정답 여부 결정
        is_correct = similarity_score >= self.similarity_threshold

        result_text = "정답" if is_correct else "오답"
        print(f"[SimilarityService] 판정: {result_text} (임계값: {self.similarity_threshold})")

        return is_correct, similarity_score

    def set_threshold(self, threshold: float) -> None:
        if not 0.0 <= threshold <= 1.0:
            raise ValueError("임계값은 0.0과 1.0 사이여야 합니다.")

        self.similarity_threshold = threshold
        print(f"[SimilarityService] 임계값 변경: {threshold}")
