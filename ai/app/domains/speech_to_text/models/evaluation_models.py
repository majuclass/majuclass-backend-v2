from dataclasses import dataclass, asdict
from typing import List, Optional, Dict, Any


@dataclass
class EvaluationRequest:
    """
    평가 요청 DTO
    Router → Facade 진입 시 사용
    """
    user_id: int
    session_id: int
    seq_no: int
    audio_s3_key: str
    language: str = "ko"


@dataclass
class EvaluationContext:
    """
    평가 컨텍스트
    평가에 필요한 모든 정보를 담는 컨테이너
    """
    session_id: int
    seq_id: int
    audio_s3_key: str
    answer_text: str
    language: str
    metadata: Dict[str, Any]  # scenario_id, student_id 등


@dataclass
class PreprocessingResult:
    """
    전처리 결과
    정규화 및 키워드 추출 결과
    """
    original_stt: str
    original_answer: str
    normalized_stt: str
    normalized_answer: str
    stt_keywords: List[str]
    answer_keywords: List[str]

    def to_dict(self) -> dict:
        return asdict(self)


@dataclass
class Scores:
    """
    다차원 점수
    3축 점수 + 가중 평균
    """
    semantic: float      # 의미 유사도 (0~1)
    keyword: float       # 키워드 매칭 (0~1)
    phonetic: float      # 발음 정확도 (0~1)
    weighted: float      # 가중 평균 (0~1)

    def to_dict(self) -> dict:
        return asdict(self)


@dataclass
class Evaluation:
    """
    평가 결과
    Pipeline 실행 후 생성되는 평가 결과
    """
    normalized_stt: str
    normalized_answer: str
    scores: Scores
    is_correct: bool
    feedback: str
    debug_info: dict

    def to_dict(self) -> dict:
        result = asdict(self)
        result['scores'] = self.scores.to_dict()
        return result


@dataclass
class EvaluationResult:
    """
    최종 결과
    DB 저장 후 반환되는 최종 결과
    """
    answer_id: int
    session_id: int
    seq_id: int
    transcribed_text: str
    answer_text: str
    scores: Scores
    is_correct: bool
    feedback: str
    attempt_no: int

    def to_dict(self) -> dict:
        result = asdict(self)
        result['scores'] = self.scores.to_dict()
        return result


# 예외 클래스들
class AudioNotFoundError(Exception):
    """오디오 파일이 존재하지 않을 때"""
    pass


class STTTimeoutError(Exception):
    """STT 처리 타임아웃"""
    pass


class EvaluationError(Exception):
    """평가 프로세스 일반 에러"""
    pass
