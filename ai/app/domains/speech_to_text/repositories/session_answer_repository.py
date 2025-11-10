from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.domains.speech_to_text.models.db_models import SessionAnswer


class SessionAnswerRepository:
    """세션 답변 관련 Repository"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_next_attempt_no(
        self,
        session_id: int,
        seq_id: int
    ) -> int:
        """
        다음 attempt_no 계산

        같은 session_id + seq_id 조합에서 최대 attempt_no를 찾아 +1

        Args:
            session_id: 세션 ID
            seq_id: 시퀀스 ID

        Returns:
            다음 attempt_no (1부터 시작)
        """
        result = await self.db.execute(
            select(func.coalesce(func.max(SessionAnswer.attempt_no), 0) + 1)
            .where(
                SessionAnswer.session_id == session_id,
                SessionAnswer.seq_id == seq_id
            )
        )

        return result.scalar_one()

    async def save_answer(
        self,
        session_id: int,
        seq_id: int,
        transcribed_text: str,
        expected_text: str,
        similarity_score: float,
        is_correct: bool,
        audio_s3_key: Optional[str] = None
    ) -> SessionAnswer:
        """
        답변 저장

        Args:
            session_id: 세션 ID
            seq_id: 시퀀스 ID
            transcribed_text: STT 변환된 텍스트
            expected_text: 정답 텍스트
            similarity_score: 유사도 점수 (0.0 ~ 1.0)
            is_correct: 정답 여부
            audio_s3_key: S3 녹음 파일 키 (선택사항)

        Returns:
            저장된 SessionAnswer 객체

        Raises:
            DatabaseError: DB 저장 실패
        """
        try:
            # 다음 attempt_no 계산
            attempt_no = await self.get_next_attempt_no(session_id, seq_id)

            # SessionAnswer 생성
            answer = SessionAnswer(
                session_id=session_id,
                seq_id=seq_id,
                answer_s3_key=audio_s3_key,
                is_correct=is_correct,
                attempt_no=attempt_no,
                transcribed_text=transcribed_text,
                expected_text=expected_text,
                similarity_score=similarity_score
            )

            self.db.add(answer)
            await self.db.commit()
            await self.db.refresh(answer)

            return answer

        except Exception as e:
            await self.db.rollback()
            raise DatabaseError(f"답변 저장 실패: {str(e)}")

    async def get_answer_by_id(
        self,
        answer_id: int
    ) -> Optional[SessionAnswer]:
        """
        ID로 답변 조회

        Args:
            answer_id: 답변 ID

        Returns:
            SessionAnswer 객체 또는 None
        """
        result = await self.db.execute(
            select(SessionAnswer)
            .where(SessionAnswer.id == answer_id)
        )

        return result.scalar_one_or_none()

    async def get_latest_answer(
        self,
        session_id: int,
        seq_id: int
    ) -> Optional[SessionAnswer]:
        """
        최신 답변 조회 (가장 높은 attempt_no)

        Args:
            session_id: 세션 ID
            seq_id: 시퀀스 ID

        Returns:
            SessionAnswer 객체 또는 None
        """
        result = await self.db.execute(
            select(SessionAnswer)
            .where(
                SessionAnswer.session_id == session_id,
                SessionAnswer.seq_id == seq_id
            )
            .order_by(SessionAnswer.attempt_no.desc())
            .limit(1)
        )

        return result.scalar_one_or_none()
