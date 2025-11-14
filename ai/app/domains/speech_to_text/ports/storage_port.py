"""
StoragePort: S3 스토리지 인터페이스

파일 스토리지 시스템(S3)과의 통신을 추상화합니다.
"""

from abc import ABC, abstractmethod


class StoragePort(ABC):
    """파일 스토리지 추상 인터페이스"""

    @abstractmethod
    async def exists(self, s3_key: str) -> bool:
        """
        파일 존재 여부 확인

        Args:
            s3_key: S3 객체 키

        Returns:
            파일이 존재하면 True, 없으면 False
        """
        pass

    @abstractmethod
    async def download(self, s3_key: str) -> bytes:
        """
        파일 다운로드

        Args:
            s3_key: S3 객체 키

        Returns:
            파일 데이터 (바이트)

        Raises:
            FileNotFoundError: 파일이 존재하지 않을 때
            Exception: 다운로드 실패 시
        """
        pass

    @abstractmethod
    async def get_presigned_url(self, s3_key: str, operation: str = "getObject") -> str:
        """
        Presigned URL 생성

        Args:
            s3_key: S3 객체 키
            operation: 작업 타입 ("getObject", "putObject")

        Returns:
            Presigned URL
        """
        pass
