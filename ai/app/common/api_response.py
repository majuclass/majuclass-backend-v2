from typing import Optional, TypeVar, Generic
from pydantic import BaseModel

T = TypeVar('T')

class ApiResponse(BaseModel, Generic[T]):
    status: str
    message: str
    data: Optional[T] = None

    @staticmethod
    def success(message: str = "요청이 성공적으로 처리되었습니다", data: Optional[T] = None):
        return ApiResponse(status="SUCCESS", message=message, data=data)

    @staticmethod
    def fail(message: str = "요청 처리 중 오류가 발생했습니다", data: Optional[T] = None):
        return ApiResponse(status="ERROR", message=message, data=data)
