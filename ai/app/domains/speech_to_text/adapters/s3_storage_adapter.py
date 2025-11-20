import requests
from app.domains.speech_to_text.ports.storage_port import StoragePort
from app.domains.speech_to_text.utils.s3_downloader import S3Downloader
from app.domains.speech_to_text.utils.presigned_url_service import PresignedUrlService


class S3StorageAdapter(StoragePort):
    def __init__(
        self,
        presigned_url_service: PresignedUrlService = None,
        s3_downloader: S3Downloader = None
    ):
        self.presigned_url_service = presigned_url_service or PresignedUrlService()
        self.s3_downloader = s3_downloader or S3Downloader()

    async def exists(self, s3_key: str) -> bool:
        try:
            # Presigned URL 발급
            presigned_url = await self.get_presigned_url(s3_key, "getObject")

            # HEAD 요청으로 존재 여부 확인
            response = requests.head(presigned_url, timeout=5)

            # 200 OK면 존재, 404면 없음
            return response.status_code == 200

        except Exception as e:
            print(f"[S3StorageAdapter] 파일 존재 확인 실패: {str(e)}")
            return False

    async def download(self, s3_key: str) -> bytes:
        try:
            # Presigned URL 발급
            presigned_url = await self.get_presigned_url(s3_key, "getObject")

            # 파일 다운로드
            response = requests.get(presigned_url, timeout=30)
            if response.status_code == 404:
                raise FileNotFoundError(f"S3 파일을 찾을 수 없습니다: {s3_key}")
            response.raise_for_status()

            print(f"[S3StorageAdapter] 파일 다운로드 완료: {s3_key}")

            return response.content

        except requests.exceptions.Timeout:
            raise Exception(f"S3 파일 다운로드 타임아웃: {s3_key}")
        except FileNotFoundError:
            raise
        except requests.exceptions.RequestException as e:
            raise Exception(f"S3 파일 다운로드 실패: {str(e)}")
        except Exception as e:
            raise Exception(f"파일 다운로드 중 오류 발생: {str(e)}")

    async def get_presigned_url(
        self, s3_key: str, operation: str = "getObject"
    ) -> str:
        try:
            # 동기 함수를 호출 (기존 PresignedUrlService는 동기)
            presigned_url = self.presigned_url_service.get_presigned_url(
                file_name=s3_key,
                operation=operation
            )
            return presigned_url

        except Exception as e:
            raise Exception(f"Presigned URL 발급 실패: {str(e)}")
