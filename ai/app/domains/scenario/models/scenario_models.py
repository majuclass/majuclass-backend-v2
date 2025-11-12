from typing import Any, Dict, Optional, Annotated
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
