from pydantic import BaseModel, Field
from typing import Optional

class SpeechToTextRequestModel(BaseModel):
    presigned_url: str = Field(
        ...,
        description="S3에 저장된 WAV 파일에 접근할 수 있는 임시 URL",
        examples=["https://bucket-name.s3.amazonaws.com/audio.wav?X-Amz-..."]
    )

    answer: str = Field(
        ...,
        description="비교할 정답 텍스트",

        examples=["사과는 빨간색이에요"]
    )

    language: Optional[str] = Field(
        default="ko-KR",
        description="음성 언어 코드 (ko-KR: 한국어, en-US: 영어 등)"
    )

    sample_rate: Optional[int] = Field(
        default=16000,  # 기본값: 16000Hz (일반적인 음질)
        description="오디오 샘플링 레이트 (Hz 단위)"
    )