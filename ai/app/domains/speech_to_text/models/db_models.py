from sqlalchemy import Column, BigInteger, Integer, String, Boolean, DECIMAL, DateTime, Enum, Text
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime

Base = declarative_base()


class ScenarioSession(Base):
    """시나리오 세션 테이블"""
    __tablename__ = "scenario_sessions"

    id = Column(BigInteger, primary_key=True)
    student_id = Column(BigInteger, nullable=False)
    scenario_id = Column(BigInteger, nullable=False)
    session_status = Column(
        Enum('IN_PROGRESS', 'COMPLETED', 'ABANDONED', name='session_status'),
        nullable=False
    )
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class ScenarioSequence(Base):
    """시나리오 시퀀스 테이블"""
    __tablename__ = "scenario_sequences"

    id = Column(BigInteger, primary_key=True)
    scenario_id = Column(BigInteger, nullable=False)
    seq_no = Column(Integer, nullable=False)
    question = Column(String(500))
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class SeqOption(Base):
    """시퀀스 옵션 테이블 (정답 포함)"""
    __tablename__ = "seq_options"

    id = Column(BigInteger, primary_key=True)
    seq_id = Column(BigInteger, nullable=False)
    option_no = Column(Integer)
    option_text = Column(String(500))
    option_s3_key = Column(String(255))
    is_answer = Column(Boolean, default=False)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class SessionAnswer(Base):
    """세션 답변 테이블 (객관식용)"""
    __tablename__ = "session_answers"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    session_id = Column(BigInteger, nullable=False)
    seq_id = Column(BigInteger, nullable=False)
    answer_s3_key = Column(String(255), nullable=True)
    is_correct = Column(Boolean, nullable=True)
    attempt_no = Column(Integer, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class SessionSTTAnswer(Base):
    """STT 답변 테이블 (난이도 상 전용)"""
    __tablename__ = "session_stt_answers"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    session_id = Column(BigInteger, nullable=False)
    seq_id = Column(BigInteger, nullable=False)
    audio_s3_key = Column(String(255), nullable=True)
    transcribed_text = Column(Text, nullable=True, comment="STT 변환된 텍스트")
    answer_text = Column(Text, nullable=True, comment="정답 텍스트")
    similarity_score = Column(DECIMAL(5, 4), nullable=True, comment="유사도 점수 (0.0000~1.0000)")
    is_correct = Column(Boolean, nullable=True)
    attempt_no = Column(Integer, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class Student(Base):
    """학생 테이블"""
    __tablename__ = "students"

    id = Column(BigInteger, primary_key=True)
    user_id = Column(BigInteger, nullable=False)
    school_id = Column(BigInteger, nullable=False)
    name = Column(String(50))
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class User(Base):
    """사용자 테이블"""
    __tablename__ = "users"

    id = Column(BigInteger, primary_key=True)
    school_id = Column(BigInteger, nullable=False)
    username = Column(String(50))
    password = Column(String(255))
    name = Column(String(50))
    email = Column(String(200))
    role = Column(Enum('STUDENT', 'TEACHER', 'ADMIN', name='user_role'))
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
