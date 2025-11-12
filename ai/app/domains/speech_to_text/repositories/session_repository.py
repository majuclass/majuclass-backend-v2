from typing import Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.exceptions import (
    SessionNotFoundError,
    SequenceNotFoundError
)
from app.domains.speech_to_text.models.db_models import (
    ScenarioSession,
    ScenarioSequence,
    SeqOption,
    Student
)


class SessionRepository:
    """세션 관련 Repository"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def verify_session(
        self,
        session_id: int,
        user_id: int
    ) -> ScenarioSession:
        print(f"[DEBUG] 세션 검증 시작: session_id={session_id}, user_id={user_id}")

        result = await self.db.execute(
            select(ScenarioSession)
            .join(Student, ScenarioSession.student_id == Student.id)
            .where(
                ScenarioSession.id == session_id,
                Student.user_id == user_id,
                ScenarioSession.session_status == 'IN_PROGRESS',
                ScenarioSession.is_deleted == False
            )
        )

        session = result.scalar_one_or_none()

        print(f"[DEBUG] 세션 조회 결과: {session}")

        if not session:
            # 디버깅: 세션 자체가 존재하는지 확인
            check_result = await self.db.execute(
                select(ScenarioSession).where(ScenarioSession.id == session_id)
            )
            exists = check_result.scalar_one_or_none()

            if exists:
                print(f"[DEBUG] 세션은 존재하지만 조건 불일치: student_id={exists.student_id}, status={exists.session_status}, is_deleted={exists.is_deleted}")
            else:
                print(f"[DEBUG] 세션 자체가 DB에 존재하지 않음")

            raise SessionNotFoundError(
                f"세션 {session_id}를 찾을 수 없거나 접근 권한이 없습니다"
            )

        return session

    async def get_seq_id(
        self,
        scenario_id: int,
        seq_no: int
    ) -> int:
        result = await self.db.execute(
            select(ScenarioSequence.id)
            .where(
                ScenarioSequence.scenario_id == scenario_id,
                ScenarioSequence.seq_no == seq_no,
                ScenarioSequence.is_deleted == False
            )
        )

        seq_id = result.scalar_one_or_none()

        if not seq_id:
            raise SequenceNotFoundError(
                f"시퀀스를 찾을 수 없습니다 (scenario_id={scenario_id}, seq_no={seq_no})"
            )

        return seq_id

    async def get_answer_text(
        self,
        seq_id: int
    ) -> str:
        result = await self.db.execute(
            select(SeqOption.option_text)
            .where(
                SeqOption.seq_id == seq_id,
                SeqOption.is_answer == True,
                SeqOption.is_deleted == False
            )
        )

        answer_text = result.scalar_one_or_none()

        if not answer_text:
            raise SequenceNotFoundError(
                f"정답을 찾을 수 없습니다 (seq_id={seq_id})"
            )

        return answer_text

    async def get_question(
        self,
        seq_id: int
    ) -> Optional[str]:
        result = await self.db.execute(
            select(ScenarioSequence.question)
            .where(
                ScenarioSequence.id == seq_id,
                ScenarioSequence.is_deleted == False
            )
        )

        return result.scalar_one_or_none()
