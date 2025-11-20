from typing import List, Dict, Any, Optional
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from .embedding_service import EmbeddingService


class ScenarioRAGService:
    """
    ChromaDB 기반 시나리오 RAG 검색 서비스

    MySQL에 저장된 시나리오 데이터를 ChromaDB로 임베딩하여
    유사한 시나리오 청크를 검색하고 참고 자료로 활용
    """

    def __init__(self, persist_directory: str = "./data/chroma_db"):
        self.embedding_service = EmbeddingService()
        self.persist_directory = persist_directory
        self._vectorstore = None

    def _get_vectorstore(self) -> Chroma:
        """Lazy loading으로 ChromaDB 로드"""
        if self._vectorstore is None:
            self._vectorstore = Chroma(
                persist_directory=self.persist_directory,
                embedding_function=self.embedding_service.get_embeddings(),
                collection_name="scenario_kb"
            )
        return self._vectorstore

    async def retrieve(
        self,
        query: str,
        category_id: int,
        top_k: int = 5,
        difficulty: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """
        쿼리에 대한 유사 시나리오 청크 검색

        Args:
            query: 검색 쿼리 (예: "영화관에서 팝콘 주문하기")
            category_id: 카테고리 필터
            top_k: 반환할 최대 결과 수
            difficulty: 난이도 필터 (옵션)

        Returns:
            [
                {
                    "content": "질문: ...",
                    "score": 0.85,
                    "metadata": {"scenario_id": 123, ...}
                },
                ...
            ]
        """
        try:
            vectorstore = self._get_vectorstore()

            # 메타데이터 필터 구성 (ChromaDB는 문자열 값 필요)
            filter_dict: Dict[str, str] = {"category_id": str(category_id)}
            if difficulty:
                filter_dict["difficulty"] = difficulty

            # 유사도 검색
            results: List[tuple[Document, float]] = vectorstore.similarity_search_with_score(
                query=query,
                k=top_k,
                filter=filter_dict
            )

            # 결과 포맷팅
            formatted_results = []
            for doc, score in results:
                formatted_results.append({
                    "content": doc.page_content,
                    "score": float(score),
                    "metadata": doc.metadata
                })

            return formatted_results

        except Exception as e:
            print(f"[RAG] 검색 실패: {e}")
            return []  # Graceful fallback