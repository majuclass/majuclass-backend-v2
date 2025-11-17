import hashlib
from typing import Optional, Union

import numpy as np
from sentence_transformers import SentenceTransformer

from app.domains.speech_to_text.ports.cache_port import CachePort


def _cosine_similarity(
        embedding1: Union[np.ndarray, list],
    embedding2: Union[np.ndarray, list]
) -> float:
    # numpy array로 변환
    if isinstance(embedding1, list):
        embedding1 = np.array(embedding1)
    if isinstance(embedding2, list):
        embedding2 = np.array(embedding2)

    # 코사인 유사도 계산
    # cos_sim = (A · B) / (||A|| * ||B||)
    dot_product = np.dot(embedding1, embedding2)
    norm1 = np.linalg.norm(embedding1)
    norm2 = np.linalg.norm(embedding2)

    if norm1 == 0 or norm2 == 0:
        return 0.0

    similarity = dot_product / (norm1 * norm2)

    return float(similarity)


class SemanticSimilarity:
    def __init__(
        self,
        model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS(하드코딩 이름 지정)",
        cache_port: Optional[CachePort] = None
    ):
        print(f"[SemanticSimilarity] 모델 로딩 중: {model_name}")
        self.model = SentenceTransformer(model_name)
        self.cache_port = cache_port
        self.model_name = model_name
        print(f"[SemanticSimilarity] 모델 로딩 완료")

    def calculate_similarity(
        self,
        text1: str,
        text2: str,
        use_cache: bool = True
    ) -> float:
        # 임베딩 생성
        embedding1 = self._encode(text1, use_cache=False)
        embedding2 = self._encode(text2, use_cache=use_cache)

        # 코사인 유사도 계산
        similarity = _cosine_similarity(embedding1, embedding2)

        print(f"[SemanticSimilarity] 유사도: {similarity:.4f}")
        print(f"  - 텍스트1: {text1}")
        print(f"  - 텍스트2: {text2}")

        return float(similarity)

    async def calculate_similarity_async(
        self,
        text1: str,
        text2: str,
        use_cache: bool = True
    ) -> float:
        # 캐시 사용 시도
        if use_cache and self.cache_port:
            embedding2 = await self._get_cached_embedding(text2)
        else:
            embedding2 = self._encode_direct(text2)

        embedding1 = self._encode_direct(text1)

        similarity = _cosine_similarity(embedding1, embedding2)

        print(f"[SemanticSimilarity] 유사도 (async): {similarity:.4f}")

        return float(similarity)

    def encode(self, text: str, use_cache: bool = False) -> np.ndarray:
        return self._encode(text, use_cache=use_cache)

    def _encode(self, text: str, use_cache: bool = False) -> np.ndarray:
        if not text:
            return np.zeros(self.model.get_sentence_embedding_dimension())

        if use_cache and self.cache_port:
            cache_key = self._make_cache_key(text)
            pass

        return self._encode_direct(text)

    def _encode_direct(self, text: str) -> np.ndarray:
        embedding = self.model.encode(text, convert_to_tensor=False)
        return embedding

    async def _get_cached_embedding(self, text: str) -> np.ndarray:
        if not self.cache_port:
            return self._encode_direct(text)

        cache_key = self._make_cache_key(text)

        # 캐시 확인
        cached = await self.cache_port.get(cache_key)

        if cached is not None:
            print(f"[SemanticSimilarity] 캐시 히트: {cache_key[:20]}...")
            return np.array(cached)

        # 캐시 미스: 새로 인코딩
        print(f"[SemanticSimilarity] 캐시 미스: {cache_key[:20]}...")
        embedding = self._encode_direct(text)

        # 캐시에 저장
        await self.cache_port.set(
            cache_key,
            embedding.tolist(),
            ttl=86400
        )

        return embedding

    def _make_cache_key(self, text: str) -> str:

        # 텍스트 + 모델명으로 해시 생성
        content = f"{self.model_name}:{text}"
        hash_value = hashlib.md5(content.encode()).hexdigest()
        return f"embedding:{hash_value}"

    def get_embedding_dimension(self) -> int:
        return self.model.get_sentence_embedding_dimension()


def calculate_semantic_similarity(
    text1: str,
    text2: str,
    model_name: str = "jhgan/ko-sroberta-multitask"
) -> float:
    semantic = SemanticSimilarity(model_name=model_name)
    return semantic.calculate_similarity(text1, text2, use_cache=False)
