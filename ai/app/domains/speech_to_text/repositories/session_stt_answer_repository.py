from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from typing import Optional

from app.domains.speech_to_text.models.db_models import SessionSTTAnswer
from app.common.exceptions import DatabaseError


class SessionSTTAnswerRepository:
    """STT 답변 관련 Repository"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_next_attempt_no(
        self,
        session_id: int,
        seq_id: int
    ) -> int:
        result = await self.db.execute(
            select(func.coalesce(func.max(SessionSTTAnswer.attempt_no), 0) + 1)
            .where(
                SessionSTTAnswer.session_id == session_id,
                SessionSTTAnswer.seq_id == seq_id
            )
        )

        return result.scalar_one()

    async def save_answer(
        self,
        session_id: int,
        seq_id: int,
        transcribed_text: str,
        answer_text: str,
        similarity_score: float,
        is_correct: bool,
        audio_s3_key: Optional[str] = None
    ) -> SessionSTTAnswer:
        try:
            # 다음 attempt_no 계산
            attempt_no = await self.get_next_attempt_no(session_id, seq_id)

            # SessionSTTAnswer 생성
            answer = SessionSTTAnswer(
                session_id=session_id,
                seq_id=seq_id,
                audio_s3_key=audio_s3_key,
                transcribed_text=transcribed_text,
                answer_text=answer_text,
                similarity_score=similarity_score,
                is_correct=is_correct,
                attempt_no=attempt_no
            )

            self.db.add(answer)
            await self.db.commit()
            await self.db.refresh(answer)

            return answer

        except Exception as e:
            await self.db.rollback()
            raise DatabaseError(f"STT 답변 저장 실패: {str(e)}")

    async def get_answer_by_id(
        self,
        answer_id: int
    ) -> Optional[SessionSTTAnswer]:
        result = await self.db.execute(
            select(SessionSTTAnswer)
            .where(SessionSTTAnswer.id == answer_id)
        )

        return result.scalar_one_or_none()

    async def get_latest_answer(
        self,
        session_id: int,
        seq_id: int
    ) -> Optional[SessionSTTAnswer]:
        result = await self.db.execute(
            select(SessionSTTAnswer)
            .where(
                SessionSTTAnswer.session_id == session_id,
                SessionSTTAnswer.seq_id == seq_id
            )
            .order_by(SessionSTTAnswer.attempt_no.desc())
            .limit(1)
        )

        return result.scalar_one_or_none()

    async def get_all_answers_by_session(
        self,
        session_id: int
    ) -> list[SessionSTTAnswer]:
        result = await self.db.execute(
            select(SessionSTTAnswer)
            .where(SessionSTTAnswer.session_id == session_id)
            .order_by(SessionSTTAnswer.seq_id, SessionSTTAnswer.attempt_no)
        )

        return result.scalars().all()
