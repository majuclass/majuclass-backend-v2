import io
from typing import Optional

from app.domains.speech_to_text.services.stt_service import WhisperSTTService, ClovaSTTService
from app.common.exceptions import STTServiceError


## StreamingSTTService: BytesIO를 그대로 STT 서비스에 전달
class StreamingSTTService:
    def __init__(self, api_key: Optional[str] = None, use_clova: bool = False):
        if use_clova:
            self.stt_service = ClovaSTTService()
            print("[StreamingSTT] Clova STT 사용")
        else:
            self.stt_service = WhisperSTTService(api_key)
            print("[StreamingSTT] Whisper STT 사용")

    async def transcribe(
        self,
        audio_buffer: io.BytesIO,
        language: str = "ko",
        prompt: str = ""
    ) -> str:
        try:
            if prompt:
                print(f"[StreamingSTT] STT 변환 시작 (언어: {language}, 프롬프트: {prompt[:50]}...)")
            else:
                print(f"[StreamingSTT] STT 변환 시작 (언어: {language})")

            # BytesIO를 그대로 STT 서비스에 전달
            transcribed_text = self.stt_service.transcribe_audio(
                audio_data=audio_buffer,
                language=language,
                prompt=prompt
            )

            print(f"[StreamingSTT] STT 변환 완료: {transcribed_text}")

            return transcribed_text

        except Exception as e:
            raise STTServiceError(f"STT 변환 실패: {str(e)}")

    def normalize_text(self, text: str) -> str:
        # 공백 제거
        normalized = text.replace(" ", "")

        # 쉼표, 마침표 등 제거
        normalized = normalized.replace(",", "").replace(".", "")
        normalized = normalized.replace("?", "").replace("!", "")

        # 소문자 변환
        normalized = normalized.lower()

        return normalized
