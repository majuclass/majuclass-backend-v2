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

    class Config:
        json_schema_extra = {
            "example": {
                "optionNo": 1,
                "optionText": "안녕하세요",
                "optionS3Key": "scenarios/options/uuid-example.jpg",
                "isAnswer": True
            }
        }

class SequenceData(BaseModel):
    seqNo: int
    question: str
    options: List[OptionData]

    class Config:
        json_schema_extra = {
            "example": {
                "seqNo": 1,
                "question": "직원에게 어떻게 인사하시겠습니까?",
                "options": [
                    {
                        "optionNo": 1,
                        "optionText": "안녕하세요",
                        "optionS3Key": "scenarios/options/uuid-1.jpg",
                        "isAnswer": True
                    },
                    {
                        "optionNo": 2,
                        "optionText": "안녕히계세요",
                        "optionS3Key": "scenarios/options/uuid-2.jpg",
                        "isAnswer": False
                    }
                ]
            }
        }

class AutoCreateScenarioResponse(BaseModel):
    title: str
    summary: str
    categoryId: int
    thumbnailS3Key: str
    backgroundS3Key: str
    sequences: List[SequenceData]

    class Config:
        json_schema_extra = {
            "example": {
                "title": "영화관에서 팝콘 구매하기",
                "summary": "영화관에서 매장 직원에게 팝콘을 구매해보아요",
                "categoryId": 1,
                "thumbnailS3Key": "scenarios/thumbnails/uuid.jpg",
                "backgroundS3Key": "scenarios/backgrounds/uuid.jpg",
                "sequences": [
                    {
                        "seqNo": 1,
                        "question": "직원에게 어떻게 인사하시겠습니까?",
                        "options": [
                            {
                                "optionNo": 1,
                                "optionText": "안녕하세요",
                                "optionS3Key": "scenarios/options/uuid-1.jpg",
                                "isAnswer": True
                            },
                            {
                                "optionNo": 2,
                                "optionText": "안녕히계세요",
                                "optionS3Key": "scenarios/options/uuid-2.jpg",
                                "isAnswer": False
                            }
                        ]
                    }
                ]
            }
        } 
