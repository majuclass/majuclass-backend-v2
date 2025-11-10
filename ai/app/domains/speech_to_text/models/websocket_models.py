from typing import Literal

from pydantic import BaseModel, Field


class AudioChunkMessage(BaseModel):
    """오디오 청크 메시지 (클라이언트 → AI)"""
    type: Literal["audio_chunk"]
    data: str = Field(..., description="Base64 인코딩된 오디오 데이터 (PCM16)")


class EndStreamMessage(BaseModel):
    """스트림 종료 메시지 (클라이언트 → AI)"""
    type: Literal["end_stream"]
    audio_s3_key: str = Field(None, description="S3에 업로드된 녹음 파일 키")


class TranscriptMessage(BaseModel):
    """실시간 텍스트 메시지 (AI → 클라이언트)"""
    type: Literal["transcript"]
    text: str = Field(..., description="변환된 텍스트")


class FinalResultMessage(BaseModel):
    """최종 결과 메시지 (AI → 클라이언트)"""
    type: Literal["final_result"]
    session_stt_answer_id: int = Field(..., description="저장된 답변 ID")
    transcribed_text: str = Field(..., description="전체 변환된 텍스트")
    answer_text: str = Field(..., description="정답 텍스트")
    similarity_score: float = Field(..., description="유사도 점수 (0.0~1.0)")
    is_correct: bool = Field(..., description="정답 여부")
    attempt_no: int = Field(..., description="시도 횟수")


class ErrorMessage(BaseModel):
    """에러 메시지 (AI → 클라이언트)"""
    type: Literal["error"]
    message: str = Field(..., description="에러 메시지")
