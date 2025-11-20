import os
from langchain_openai import OpenAIEmbeddings


class EmbeddingService:
    """OpenAI Embedding 서비스 (GMS API 사용)"""

    def __init__(self):
        self.embeddings = OpenAIEmbeddings(
            model="text-embedding-3-small",
            base_url=os.getenv("GMS_BASE"),
            api_key=os.getenv("GMS_KEY")
        )

    def get_embeddings(self):
        """LangChain Embeddings 객체 반환"""
        return self.embeddings
