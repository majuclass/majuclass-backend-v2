from typing import Any, Dict, Optional, Annotated, List
from pydantic import BaseModel, Field

class GenerateScenarioRequest(BaseModel):
    prompt: str
    question_count: Annotated[int, Field(ge=1, le=20)]
    options_per_question: Annotated[int, Field(ge=2, le=6)]
    categoryId: int
    image_size: Optional[str] = "1024x1024"
    option_image_size: Optional[str] = "512x512"

class GenerateScenarioResponse(BaseModel):
    # LLM 결과 + 주입된 S3 키들을 그대로 딤기
    scenario: Dict[str, Any]
    uploadSummary: Dict[str, Any]

class AutoCreateScenarioRequest(BaseModel):
    category_id: int
    seq_cnt: Annotated[int, Field(ge=1, le=20)]
    option_cnt: Annotated[int, Field(ge=1, le=6)]
    prompt: str

class OptionData(BaseModel):
    optionNo: int
    optionText: str
    optionS3Key: str
    isAnswer: bool

class SequenceData(BaseModel):
    seqNo: int
    question: str
    options: List[OptionData]

class AutoCreateScenarioResponse(BaseModel):
    title: str
    summary: str
    categoryId: int
    thumbnailS3Key: str
    backgroundS3Key: str
    sequences: List[SequenceData] 
