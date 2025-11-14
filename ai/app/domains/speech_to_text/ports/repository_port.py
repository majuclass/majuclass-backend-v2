"""
RepositoryPort: 데이터베이스 인터페이스

데이터베이스와의 통신을 추상화합니다.
"""

from abc import ABC, abstractmethod
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from dataclasses import dataclass


@dataclass
class SessionData:
    """세션 데이터"""
    id: int
    student_id: int
    scenario_id: int
    session_status: str


@dataclass
class StudentProfile:
    """학생 프로필"""
    id: int
    user_id: int
    name: str
    grade: Optional[int] = None


@dataclass
class SavedAnswer:
    """저장된 답변 데이터"""
    id: int
    session_id: int
    seq_id: int
    attempt_no: int
    feedback_text: str


class RepositoryPort(ABC):
    """데이터베이스 추상 인터페이스"""

    @abstractmethod
    async def verify_session(
        self, session_id: int, user_id: int, db: AsyncSession
    ) -> SessionData:
        """
        세션 검증 및 조회

        Args:
            session_id: 세션 ID
            user_id: 사용자 ID
            db: 데이터베이스 세션

        Returns:
            SessionData: 세션 정보

        Raises:
            SessionNotFoundError: 세션이 존재하지 않을 때
            PermissionDeniedError: 권한이 없을 때
        """
        pass

    @abstractmethod
    async def get_seq_id(
        self, scenario_id: int, seq_no: int, db: AsyncSession
    ) -> int:
        """
        시퀀스 ID 조회

        Args:
            scenario_id: 시나리오 ID
            seq_no: 시퀀스 번호
            db: 데이터베이스 세션

        Returns:
            seq_id: 시퀀스 ID

        Raises:
            SequenceNotFoundError: 시퀀스가 존재하지 않을 때
        """
        pass

    @abstractmethod
    async def get_answer_text(self, seq_id: int, db: AsyncSession) -> str:
        """
        정답 텍스트 조회

        Args:
            seq_id: 시퀀스 ID
            db: 데이터베이스 세션

        Returns:
            정답 텍스트

        Raises:
            AnswerNotFoundError: 정답이 존재하지 않을 때
        """
        pass

    @abstractmethod
    async def get_student_profile(
        self, user_id: int, db: AsyncSession
    ) -> Optional[StudentProfile]:
        """
        학생 프로필 조회

        Args:
            user_id: 사용자 ID
            db: 데이터베이스 세션

        Returns:
            StudentProfile 또는 None
        """
        pass

    @abstractmethod
    async def save_evaluation(
        self,
        session_id: int,
        seq_id: int,
        audio_s3_key: str,
        transcribed_text: str,
        answer_text: str,
        weighted_score: float,
        is_correct: bool,
        feedback_text: str,
        db: AsyncSession,
    ) -> SavedAnswer:
        """
        평가 결과 저장

        Args:
            session_id: 세션 ID
            seq_id: 시퀀스 ID
            audio_s3_key: 오디오 S3 키
            transcribed_text: STT 변환 텍스트
            answer_text: 정답 텍스트
            weighted_score: 가중 평균 점수
            is_correct: 정답 여부
            feedback_text: 피드백 메시지
            db: 데이터베이스 세션

        Returns:
            SavedAnswer: 저장된 답변 정보
        """
        pass
