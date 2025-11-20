"""
CachePort: 캐시 인터페이스

캐시 시스템(Redis 등)과의 통신을 추상화합니다.
선택적으로 사용 가능합니다.
"""

from abc import ABC, abstractmethod
from typing import Any, Optional


class CachePort(ABC):
    """캐시 추상 인터페이스"""

    @abstractmethod
    async def get(self, key: str) -> Optional[Any]:
        """
        캐시에서 값 조회

        Args:
            key: 캐시 키

        Returns:
            캐시된 값 또는 None (없으면)
        """
        pass

    @abstractmethod
    async def set(self, key: str, value: Any, ttl: int = 3600) -> None:
        """
        캐시에 값 저장

        Args:
            key: 캐시 키
            value: 저장할 값
            ttl: 만료 시간 (초, 기본값: 1시간)
        """
        pass

    @abstractmethod
    async def delete(self, key: str) -> None:
        """
        캐시에서 값 삭제

        Args:
            key: 캐시 키
        """
        pass

    @abstractmethod
    async def exists(self, key: str) -> bool:
        """
        캐시 키 존재 여부 확인

        Args:
            key: 캐시 키

        Returns:
            존재하면 True, 없으면 False
        """
        pass
