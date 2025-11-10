class BaseSTTException(Exception):
    """STT 관련 기본 예외"""
    def __init__(self, message: str, code: str):
        self.message = message
        self.code = code
        super().__init__(self.message)


class AuthenticationError(BaseSTTException):
    """인증 실패"""
    def __init__(self, message: str = "인증에 실패했습니다"):
        super().__init__(message, "AUTHENTICATION_FAILED")


class SessionNotFoundError(BaseSTTException):
    """세션을 찾을 수 없음"""
    def __init__(self, message: str = "세션을 찾을 수 없습니다"):
        super().__init__(message, "SESSION_NOT_FOUND")


class PermissionDeniedError(BaseSTTException):
    """권한 없음"""
    def __init__(self, message: str = "접근 권한이 없습니다"):
        super().__init__(message, "PERMISSION_DENIED")


class SequenceNotFoundError(BaseSTTException):
    """시퀀스를 찾을 수 없음"""
    def __init__(self, message: str = "시퀀스를 찾을 수 없습니다"):
        super().__init__(message, "SEQUENCE_NOT_FOUND")


class STTServiceError(BaseSTTException):
    """STT 서비스 에러"""
    def __init__(self, message: str = "음성 변환에 실패했습니다"):
        super().__init__(message, "STT_SERVICE_ERROR")


class DatabaseError(BaseSTTException):
    """데이터베이스 에러"""
    def __init__(self, message: str = "데이터베이스 처리 중 오류가 발생했습니다"):
        super().__init__(message, "DATABASE_ERROR")
