import requests
from typing import Optional, Union
import os
import io
import tempfile

class ClovaSTTService:
    def __init__(
        self,
        client_id: Optional[str] = None,
        client_secret: Optional[str] = None
    ):
        self.client_id = client_id or os.getenv("CLOVA_CLIENT_ID")
        self.client_secret = client_secret or os.getenv("CLOVA_CLIENT_SECRET")

        if not self.client_id or not self.client_secret:
            raise ValueError(
                "Clova API 인증 정보가 필요합니다. "
                "환경변수 CLOVA_CLIENT_ID, CLOVA_CLIENT_SECRET을 설정하거나 "
                "생성자에 전달해주세요."
            )

        # Clova STT API 엔드포인트
        self.api_url = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt"

    def transcribe_audio(
        self,
        audio_data: Union[str, io.BytesIO],
        language: str = "Kor",
        prompt: str = ""
    ) -> str:
        # Clova는 prompt를 지원하지 않으므로 무시
        if prompt:
            print(f"[ClovaSTT] 경고: Clova API는 프롬프트를 지원하지 않습니다 (무시됨)")
        temp_file_path = None

        try:
            print(f"[ClovaSTT] 음성 변환 시작")
            print(f"[ClovaSTT] 언어: {language}")

            # 언어 파라미터 검증
            valid_languages = ["Kor", "Eng", "Jpn", "Chn"]
            if language not in valid_languages:
                raise ValueError(f"지원하지 않는 언어입니다: {language}. 지원 언어: {valid_languages}")

            # 1단계: 오디오 데이터 읽기
            if isinstance(audio_data, str):
                # 파일 경로인 경우
                print(f"[ClovaSTT] 파일 경로에서 읽기: {audio_data}")
                with open(audio_data, 'rb') as audio_file:
                    binary_data = audio_file.read()
            elif isinstance(audio_data, io.BytesIO):
                # BytesIO인 경우 - 임시 파일로 저장 (WAV → WAV)
                print(f"[ClovaSTT] BytesIO에서 임시 WAV 파일 생성")
                with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
                    audio_data.seek(0)
                    temp_file.write(audio_data.read())
                    temp_file_path = temp_file.name

                # 임시 파일 읽기
                with open(temp_file_path, 'rb') as audio_file:
                    binary_data = audio_file.read()
            else:
                raise ValueError(f"지원하지 않는 오디오 데이터 타입: {type(audio_data)}")

            # 2단계: Clova STT API 호출
            headers = {
                'X-NCP-APIGW-API-KEY-ID': self.client_id,
                'X-NCP-APIGW-API-KEY': self.client_secret,
                'Content-Type': 'application/octet-stream'
            }

            params = {
                'lang': language
            }

            response = requests.post(
                self.api_url,
                headers=headers,
                params=params,
                data=binary_data
            )

            # 3단계: HTTP 상태 코드 확인
            response.raise_for_status()

            # 4단계: 응답에서 텍스트 추출
            response_json = response.json()

            # 디버깅: 전체 응답 출력
            print(f"[ClovaSTT] API 응답 전체: {response_json}")

            transcribed_text = response_json.get('text', '')

            # 5단계: 텍스트 정제
            transcribed_text = transcribed_text.strip()

            print(f"[ClovaSTT] 변환 완료: {transcribed_text}")

            return transcribed_text

        except FileNotFoundError:
            raise Exception(f"오디오 파일을 찾을 수 없습니다: {audio_data}")

        except requests.exceptions.HTTPError as e:
            status_code = e.response.status_code
            error_message = e.response.text

            if status_code == 401:
                raise Exception("Clova API 인증 실패. Client ID/Secret을 확인해주세요.")
            elif status_code == 429:
                raise Exception("API 호출 한도 초과. 잠시 후 다시 시도해주세요.")
            else:
                raise Exception(f"API 호출 실패 (상태 코드: {status_code}): {error_message}")

        except requests.exceptions.RequestException as e:
            raise Exception(f"네트워크 오류: {str(e)}")

        except Exception as e:
            raise Exception(f"Clova STT 변환 실패: {str(e)}")

        finally:
            # 임시 파일 정리
            if temp_file_path and os.path.exists(temp_file_path):
                try:
                    os.remove(temp_file_path)
                    print(f"[ClovaSTT] 임시 파일 삭제 완료")
                except Exception as e:
                    print(f"[ClovaSTT] 임시 파일 삭제 실패: {e}")

    def validate_audio_format(self, audio_file_path: str) -> bool:
        supported_extensions = ['.mp3', '.aac', '.ac3', '.ogg', '.flac', '.wav']

        # 파일 존재 확인
        if not os.path.exists(audio_file_path):
            return False

        # 확장자 확인
        _, ext = os.path.splitext(audio_file_path)
        return ext.lower() in supported_extensions


class WhisperSTTService:
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
        audio_data: Union[str, io.BytesIO],
        language: str = "ko",
        prompt: str = ""
    ) -> str:
        temp_file_path = None

        try:
            if prompt:
                print(f"[WhisperSTT] 음성 변환 시작 (프롬프트: {prompt[:50]}...)")
            else:
                print(f"[WhisperSTT] 음성 변환 시작")

            # 1단계: 오디오 데이터 준비
            if isinstance(audio_data, str):
                # 파일 경로인 경우
                print(f"[WhisperSTT] 파일 경로에서 읽기: {audio_data}")
                with open(audio_data, 'rb') as audio_file:
                    files = {'file': audio_file}
                    data = {
                        'model': 'whisper-1',
                        'language': language
                    }
                    # 프롬프트 추가 (있을 경우)
                    if prompt:
                        data['prompt'] = prompt
                    headers = {
                        'Authorization': f'Bearer {self.api_key}'
                    }

                    response = requests.post(
                        self.api_url,
                        headers=headers,
                        files=files,
                        data=data
                    )

            elif isinstance(audio_data, io.BytesIO):
                # BytesIO인 경우 - 임시 파일로 저장
                print(f"[WhisperSTT] BytesIO에서 임시 WAV 파일 생성")
                with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
                    audio_data.seek(0)
                    temp_file.write(audio_data.read())
                    temp_file_path = temp_file.name

                # 임시 파일로 API 호출
                with open(temp_file_path, 'rb') as audio_file:
                    files = {'file': audio_file}
                    data = {
                        'model': 'whisper-1',
                        'language': language
                    }
                    # 프롬프트 추가 (있을 경우)
                    if prompt:
                        data['prompt'] = prompt
                    headers = {
                        'Authorization': f'Bearer {self.api_key}'
                    }

                    response = requests.post(
                        self.api_url,
                        headers=headers,
                        files=files,
                        data=data
                    )
            else:
                raise ValueError(f"지원하지 않는 오디오 데이터 타입: {type(audio_data)}")

            # 2단계: HTTP 상태 코드 확인
            response.raise_for_status()

            # 3단계: 응답에서 텍스트 추출
            response_json = response.json()

            # 디버깅: 전체 응답 출력
            print(f"[WhisperSTT] API 응답 전체: {response_json}")

            transcribed_text = response_json.get('text', '')

            # 4단계: 텍스트 정제
            transcribed_text = transcribed_text.strip()

            print(f"[WhisperSTT] 변환 완료: {transcribed_text}")

            return transcribed_text

        except FileNotFoundError:
            raise Exception(f"오디오 파일을 찾을 수 없습니다: {audio_data}")

        except requests.exceptions.HTTPError as e:
            status_code = e.response.status_code
            if status_code == 401:
                raise Exception("GMS API 인증 실패. API 키를 확인해주세요.")
            elif status_code == 429:
                raise Exception("API 호출 한도 초과. 잠시 후 다시 시도해주세요.")
            else:
                raise Exception(f"API 호출 실패 (상태 코드: {status_code}): {e.response.text}")

        except requests.exceptions.RequestException as e:
            raise Exception(f"네트워크 오류: {str(e)}")

        except Exception as e:
            raise Exception(f"Whisper STT 변환 실패: {str(e)}")

        finally:
            # 임시 파일 정리
            if temp_file_path and os.path.exists(temp_file_path):
                try:
                    os.remove(temp_file_path)
                    print(f"[WhisperSTT] 임시 파일 삭제 완료")
                except Exception as e:
                    print(f"[WhisperSTT] 임시 파일 삭제 실패: {e}")

    def validate_audio_format(self, audio_file_path: str) -> bool:
        supported_extensions = ['.wav', '.mp3', '.m4a', '.webm', '.mp4', '.mpga']

        # 파일 존재 확인
        if not os.path.exists(audio_file_path):
            return False

        # 확장자 확인
        _, ext = os.path.splitext(audio_file_path)
        return ext.lower() in supported_extensions


# 기본 별칭: 기존 코드 호환성을 위해 WhisperSTTService를 기본값으로 사용
STTService = WhisperSTTService
