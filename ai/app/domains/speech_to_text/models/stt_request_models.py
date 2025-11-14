from pydantic import BaseModel, Field

class SpeechToTextRequestModel(BaseModel):
    audio_s3_key: str = Field(
        ...,
        description="S3에 위치해 있는 오디오의 경로",
        examples=["students/1/sessions/1/seq_1_attempt_1.wav"]
    )