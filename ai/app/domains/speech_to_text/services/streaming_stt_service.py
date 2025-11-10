import io
import tempfile
import os
from typing import Optional

from app.domains.speech_to_text.services.stt_service import STTService
from app.common.exceptions import STTServiceError


## whisper API 사용
class StreamingSTTService:
    def __init__(self, api_key: Optional[str] = None):
        self.stt_service = STTService(api_key)

    async def transcribe(self, audio_buffer: io.BytesIO, language: str = "ko") -> str:
        """
        오디오 버퍼를 텍스트로 변환

        Args:
            audio_buffer: WAV 형식의 오디오 데이터 (BytesIO)
            language: 언어 코드 (기본값: "ko")

        Returns:
            변환된 텍스트

        Raises:
            STTServiceError: STT 변환 실패

        동작 방식:
            1. BytesIO를 임시 파일로 저장
            2. STTService.transcribe_audio() 호출
            3. 임시 파일 삭제
            4. 결과 반환
        """
        temp_file_path = None

        try:
            # 1. 임시 파일 생성
            with tempfile.NamedTemporaryFile(
                suffix='.wav',
                delete=False
            ) as temp_file:
                # BytesIO → 파일 쓰기
                audio_buffer.seek(0)  # 포인터를 처음으로
                temp_file.write(audio_buffer.read())
                temp_file_path = temp_file.name

            # 2. STT 변환
            transcribed_text = self.stt_service.transcribe_audio(
                audio_file_path=temp_file_path,
                language=language
            )

            return transcribed_text

        except Exception as e:
            raise STTServiceError(f"STT 변환 실패: {str(e)}")

        finally:
            # 3. 임시 파일 삭제
            if temp_file_path and os.path.exists(temp_file_path):
                try:
                    os.remove(temp_file_path)
                except Exception as e:
                    print(f"[Warning] 임시 파일 삭제 실패: {e}")

    def normalize_text(self, text: str) -> str:
        """
        텍스트 정규화 (유사도 비교 전)

        Args:
            text: 원본 텍스트

        Returns:
            정규화된 텍스트

        정규화 작업:
            - 공백 제거
            - 소문자 변환 (영어)
            - 특수문자 제거
        """
        # 공백 제거
        normalized = text.replace(" ", "")

        # 쉼표, 마침표 등 제거
        normalized = normalized.replace(",", "").replace(".", "")
        normalized = normalized.replace("?", "").replace("!", "")

        # 소문자 변환
        normalized = normalized.lower()

        return normalized
