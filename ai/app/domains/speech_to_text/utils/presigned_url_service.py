import requests
from typing import Optional


LAMBDA_PRESIGNED_URL_API = os.getenv("LAMBDA_PRESIGNED_URL_API")
class PresignedUrlService:
    def __init__(self, api_endpoint: str = LAMBDA_PRESIGNED_URL_API):
        self.api_endpoint = api_endpoint

    def get_presigned_url(
        self,
        file_name: str,
        operation: str = "getObject"
    ) -> str:
        try:
            print(f"[PresignedUrlService] Presigned URL 발급 요청")
            print(f"  - fileName: {file_name}")
            print(f"  - operation: {operation}")

            # POST 요청 데이터
            request_data = {
                "fileName": file_name,
                "operation": operation
            }

            # API 호출
            response = requests.post(
                self.api_endpoint,
                json=request_data,
                timeout=10
            )

            # HTTP 상태 코드 확인
            response.raise_for_status()

            # 응답 파싱
            response_data = response.json()
            print(f"[PresignedUrlService] API 응답: {response_data}")

            # Presigned URL 추출
            presigned_url = response_data.get("url")

            if isinstance(presigned_url, dict):
                # 응답이 딕셔너리인 경우, 실제 URL 찾기
                presigned_url = presigned_url.get("url") or presigned_url.get("presignedUrl")

            if not presigned_url or not isinstance(presigned_url, str):
                raise Exception(f"유효한 Presigned URL을 받지 못했습니다: {response_data}")

            print(f"[PresignedUrlService] Presigned URL 발급 완료: {presigned_url[:50]}...")

            return presigned_url

        except requests.exceptions.Timeout:
            raise Exception("Presigned URL 발급 API 타임아웃 (10초 초과)")

        except requests.exceptions.HTTPError as e:
            status_code = e.response.status_code
            error_message = e.response.text
            raise Exception(f"Presigned URL 발급 실패 (HTTP {status_code}): {error_message}")

        except requests.exceptions.RequestException as e:
            raise Exception(f"Presigned URL 발급 API 호출 실패: {str(e)}")

        except Exception as e:
            raise Exception(f"Presigned URL 발급 중 오류: {str(e)}")
