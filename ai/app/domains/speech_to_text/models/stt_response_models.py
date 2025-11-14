from pydantic import BaseModel

class SpeechToTextResponse(BaseModel):
    session_stt_answer_id: int
    transcribed_text: str
    answer_text: str
    similarity_score: float
    is_correct: bool
    # feedback_message: str
    attempt_no: int