import requests
from typing import Optional
import os

class STTService:
    def __init__(self, api_key: Optional[str] = None):
        # API 키 설정: 파라미터로 받거나 환경변수에서 가져오기
        self.api_key = api_key or os.getenv("GMS_KEY")

        if not self.api_key:
            raise ValueError(
                "GMS API 키가 필요합니다. "
                "환경변수 GMS_KEY를 설정하거나 생성자에 전달해주세요."
            )

        # GMS API 엔드포인트 (OpenAI Whisper를 프록시로 사용)
        self.api_url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/audio/transcriptions"

    def transcribe_audio(
        self,
        audio_file_path: str,
        language: str = "ko"
    ) -> str:

        try:
            print(f"[STTService] 음성 파일 변환 시작: {audio_file_path}")

            # 1단계: 오디오 파일 열기
            # 'rb' = Read Binary (바이너리 읽기 모드)
            # 음성 파일은 텍스트가 아닌 바이너리 데이터이므로 'rb' 사용
            with open(audio_file_path, 'rb') as audio_file:
                # 2단계: GMS API를 통해 OpenAI Whisper API 호출
                # multipart/form-data 형식으로 요청
                files = {
                    'file': audio_file
                }
                data = {
                    'model': 'whisper-1',
                    'language': language
                }
                headers = {
                    'Authorization': f'Bearer {self.api_key}'
                }

                response = requests.post(
                    self.api_url,
                    headers=headers,
                    files=files,
                    data=data
                )

            # 3단계: HTTP 상태 코드 확인
            response.raise_for_status()

            # 4단계: 응답에서 텍스트 추출
            response_json = response.json()
            transcribed_text = response_json.get('text', '')

            # 5단계: 텍스트 정제
            # strip(): 앞뒤 공백 제거
            transcribed_text = transcribed_text.strip()

            print(f"[STTService] 변환 완료: {transcribed_text}")

            return transcribed_text

        except FileNotFoundError:
            # 파일을 찾을 수 없는 경우
            raise Exception(f"오디오 파일을 찾을 수 없습니다: {audio_file_path}")

        except requests.exceptions.HTTPError as e:
            # HTTP 에러 (4xx, 5xx)
            status_code = e.response.status_code
            if status_code == 401:
                raise Exception("GMS API 인증 실패. API 키를 확인해주세요.")
            elif status_code == 429:
                raise Exception("API 호출 한도 초과. 잠시 후 다시 시도해주세요.")
            else:
                raise Exception(f"API 호출 실패 (상태 코드: {status_code}): {e.response.text}")

        except requests.exceptions.RequestException as e:
            # 네트워크 에러
            raise Exception(f"네트워크 오류: {str(e)}")

        except Exception as e:
            # 기타 에러
            raise Exception(f"STT 변환 실패: {str(e)}")

    def validate_audio_format(self, audio_file_path: str) -> bool:
        """
        오디오 파일 형식이 유효한지 검증합니다.

        Args:
            audio_file_path: 검증할 파일 경로

        Returns:
            유효하면 True, 아니면 False

        지원 형식: wav, mp3, m4a, webm, mp4, mpga

        왜 필요한가요?
        - 잘못된 파일을 API에 보내면 비용이 발생하고 에러가 남
        - 미리 검증해서 문제를 방지
        """
        supported_extensions = ['.wav', '.mp3', '.m4a', '.webm', '.mp4', '.mpga']

        # 파일 존재 확인
        if not os.path.exists(audio_file_path):
            return False

        # 확장자 확인
        _, ext = os.path.splitext(audio_file_path)
        return ext.lower() in supported_extensions
