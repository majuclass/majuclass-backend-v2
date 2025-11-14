import os
import tempfile

from app.domains.speech_to_text.ports.stt_port import STTPort, STTResult
from app.domains.speech_to_text.services.stt_service import WhisperSTTService


class WhisperSTTAdapter(STTPort):
    """Whisper STT 어댑터"""

    def __init__(self, whisper_service: WhisperSTTService = None):
        self.whisper_service = whisper_service or WhisperSTTService()

    async def transcribe(
        self, audio_data: bytes, language: str = "ko"
    ) -> STTResult:
        """
        음성을 텍스트로 변환

        bytes → 임시 파일 → Whisper API → 텍스트
        """
        temp_file_path = None

        try:
            # bytes → 임시 파일 생성
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as temp_file:
                temp_file.write(audio_data)
                temp_file_path = temp_file.name

            print(f"[WhisperSTTAdapter] 임시 파일 생성: {temp_file_path}")

            # Whisper STT 호출 (동기 함수)
            transcribed_text = self.whisper_service.transcribe_audio(
                audio_data=temp_file_path,
                language=language
            )

            # STTResult 생성
            result = STTResult(
                text=transcribed_text,
                confidence=0.95,  # Whisper는 신뢰도를 반환하지 않음 (기본값)
                duration=0.0      # 오디오 길이 계산 생략 (선택)
            )

            print(f"[WhisperSTTAdapter] STT 변환 완료: {transcribed_text}")

            return result

        except Exception as e:
            raise Exception(f"STT 변환 실패: {str(e)}")

        finally:
            # 임시 파일 정리
            if temp_file_path and os.path.exists(temp_file_path):
                try:
                    os.remove(temp_file_path)
                    print(f"[WhisperSTTAdapter] 임시 파일 삭제 완료")
                except Exception as e:
                    print(f"[WhisperSTTAdapter] 임시 파일 삭제 실패: {e}")
