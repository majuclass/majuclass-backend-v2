from typing import Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.exceptions import DatabaseError
from app.domains.speech_to_text.models.db_models import SessionSTTAnswer, Student
from app.domains.speech_to_text.ports.repository_port import (
    RepositoryPort,
    SessionData,
    StudentProfile,
    SavedAnswer
)
from app.domains.speech_to_text.repositories.session_repository import SessionRepository
from app.domains.speech_to_text.repositories.session_stt_answer_repository import SessionSTTAnswerRepository


class DBRepositoryAdapter(RepositoryPort):
    """데이터베이스 Repository 어댑터"""

    async def verify_session(
        self, session_id: int, user_id: int, db: AsyncSession
    ) -> SessionData:
        session_repo = SessionRepository(db)
        session = await session_repo.verify_session(session_id, user_id)

        return SessionData(
            id=session.id,
            student_id=session.student_id,
            scenario_id=session.scenario_id,
            session_status=session.session_status
        )

    async def get_seq_id(
        self, scenario_id: int, seq_no: int, db: AsyncSession
    ) -> int:
        """시퀀스 ID 조회 (기존 SessionRepository 사용)"""
        session_repo = SessionRepository(db)
        return await session_repo.get_seq_id(scenario_id, seq_no)

    async def get_answer_text(self, seq_id: int, db: AsyncSession) -> str:
        """정답 텍스트 조회 (기존 SessionRepository 사용)"""
        session_repo = SessionRepository(db)
        return await session_repo.get_answer_text(seq_id)

    async def get_student_profile(
        self, user_id: int, db: AsyncSession
    ) -> Optional[StudentProfile]:
        try:
            result = await db.execute(
                select(Student).where(
                    Student.user_id == user_id,
                    Student.is_deleted == False
                )
            )
            student = result.scalar_one_or_none()

            if not student:
                return None

            return StudentProfile(
                id=student.id,
                user_id=student.user_id,
                name=student.name,
            )

        except Exception as e:
            print(f"[DBRepositoryAdapter] 학생 프로필 조회 실패: {str(e)}")
            return None

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
        try:
            answer_repo = SessionSTTAnswerRepository(db)

            # 다음 attempt_no 계산
            attempt_no = await answer_repo.get_next_attempt_no(session_id, seq_id)

            # SessionSTTAnswer 생성 (기존 DB 스키마)
            answer = SessionSTTAnswer(
                session_id=session_id,
                seq_id=seq_id,
                audio_s3_key=audio_s3_key,
                transcribed_text=transcribed_text,
                answer_text=answer_text,
                similarity_score=weighted_score,
                is_correct=is_correct,
                attempt_no=attempt_no
            )

            db.add(answer)
            await db.commit()
            await db.refresh(answer)

            print(f"[DBRepositoryAdapter] 평가 결과 저장 완료: answer_id={answer.id}")

            return SavedAnswer(
                id=answer.id,
                session_id=answer.session_id,
                seq_id=answer.seq_id,
                attempt_no=answer.attempt_no,
                feedback_text=feedback_text
            )

        except Exception as e:
            await db.rollback()
            raise DatabaseError(f"평가 결과 저장 실패: {str(e)}")
