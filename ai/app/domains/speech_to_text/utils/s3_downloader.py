import requests
import tempfile
import os
from typing import Tuple


class S3Downloader:
    def __init__(self, timeout: int = 30):
        self.timeout = timeout

    def download_from_presigned_url(self, presigned_url: str) -> Tuple[str, str]:
        try:
            response = requests.get(presigned_url, timeout=self.timeout)
            response.raise_for_status()

            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as temp_file:
                temp_file.write(response.content)
                temp_file_path = temp_file.name
                file_name = os.path.basename(temp_file_path)

                print(f"[S3Downloader] 파일 다운로드 완료: {file_name}")

                return temp_file_path, file_name

        except requests.exceptions.Timeout:
            raise Exception(f"S3 파일 다운로드 타임아웃 ({self.timeout}초 초과)")

        except requests.exceptions.RequestException as e:
            raise Exception(f"S3 파일 다운로드 실패: {str(e)}")

        except Exception as e:
            raise Exception(f"파일 다운로드 중 오류 발생: {str(e)}")

    @staticmethod
    def cleanup_temp_file(file_path: str) -> None:
        try:
            if os.path.exists(file_path):
                os.remove(file_path)
                print(f"[S3Downloader] 임시 파일 삭제: {file_path}")
        except Exception as e:
            print(f"[S3Downloader] 임시 파일 삭제 실패: {str(e)}")
