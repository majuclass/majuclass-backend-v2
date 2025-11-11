import base64
import os

from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Query, Depends
from jose import jwt, JWTError
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.exceptions import (
    AuthenticationError,
    SessionNotFoundError,
    PermissionDeniedError,
    SequenceNotFoundError
)
from app.database import get_session
from app.domains.speech_to_text.repositories.session_repository import SessionRepository
from app.domains.speech_to_text.repositories.session_stt_answer_repository import SessionSTTAnswerRepository
from app.domains.speech_to_text.services.similarity_service import SimilarityService
from app.domains.speech_to_text.services.streaming_stt_service import StreamingSTTService
from app.domains.speech_to_text.utils.audio_buffer import AudioBuffer

router = APIRouter(prefix="/ws", tags=["WebSocket STT"])

# 환경 변수
SECRET_KEY = os.getenv("JWT_SECRET")
ALGORITHM = "HS256"
CHUNK_DURATION = 3.0
SIMILARITY_THRESHOLD = float(os.getenv("SIMILARITY_THRESHOLD", "0.7"))


async def verify_websocket_token(token: str, db: AsyncSession) -> int:
    """
    WebSocket JWT 토큰 검증

    Args:
        token: JWT 토큰
        db: DB 세션

    Returns:
        user_id

    Raises:
        AuthenticationError: 인증 실패
    """
    try:
        # JWT 디코딩
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])

        # 토큰 타입 확인
        token_type = payload.get("token_type")
        if token_type == "refresh":
            raise AuthenticationError("Refresh 토큰으로는 접근할 수 없습니다")

        # user_id 추출
        user_id = payload.get("sub")
        if not user_id:
            raise AuthenticationError("유효하지 않은 토큰입니다")

        # DB에서 사용자 확인
        from sqlalchemy import text
        result = await db.execute(
            text("SELECT id FROM users WHERE id = :user_id AND is_deleted = 0"),
            {"user_id": int(user_id)}
        )

        if not result.scalar_one_or_none():
            raise AuthenticationError("유효하지 않은 사용자입니다")

        return int(user_id)

    except JWTError:
        raise AuthenticationError("잘못된 토큰입니다")
    except AuthenticationError:
        raise
    except Exception as e:
        raise AuthenticationError(f"인증 실패: {str(e)}")


@router.websocket("/stt/{session_id}/{seq_no}")
async def websocket_stt_endpoint(
    websocket: WebSocket,
    session_id: int,
    seq_no: int,
    token: str = Query(..., description="JWT 인증 토큰"),
    db: AsyncSession = Depends(get_session)
):
    """
    WebSocket 실시간 STT 엔드포인트

    연결:
        ws://localhost:8001/ws/stt/{session_id}/{seq_no}?token={jwt_token}

    Path Parameters:
        - session_id: scenario_sessions.id
        - seq_no: 시퀀스 번호 (1, 2, 3, ...)

    Query Parameters:
        - token: JWT 인증 토큰

    메시지 프로토콜:
        클라이언트 → AI:
            - {type: "audio_chunk", data: "base64..."}
            - {type: "end_stream", audioS3Key: "..."}

        AI → 클라이언트:
            - {type: "transcript", text: "...", isFinal: false}
            - {type: "final_result", answerId: 123, ...}
            - {type: "error", message: "...", code: "..."}
    """
    await websocket.accept()

    # 초기화
    session_repo = None
    answer_repo = None
    buffer = None

    try:
        print(f"[WebSocket] 연결 수립: session_id={session_id}, seq_no={seq_no}")

        # 1. 인증
        try:
            user_id = await verify_websocket_token(token, db)
            print(f"[WebSocket] 인증 성공: user_id={user_id}")
        except AuthenticationError as e:
            await websocket.send_json({
                "type": "error",
                "message": e.message
            })
            await websocket.close()
            return

        # 2. Repository 초기화
        session_repo = SessionRepository(db)
        answer_repo = SessionSTTAnswerRepository(db)

        # 3. 세션 검증
        try:
            session = await session_repo.verify_session(session_id, user_id)
            scenario_id = session.scenario_id
            print(f"[WebSocket] 세션 검증 완료: scenario_id={scenario_id}")
        except (SessionNotFoundError, PermissionDeniedError) as e:
            await websocket.send_json({
                "type": "error",
                "message": e.message
            })
            await websocket.close()
            return

        # 4. 시퀀스 ID 및 정답 조회
        try:
            seq_id = await session_repo.get_seq_id(scenario_id, seq_no)
            answer_text = await session_repo.get_answer_text(seq_id)
            print(f"[WebSocket] 정답 조회 완료: seq_id={seq_id}")
        except SequenceNotFoundError as e:
            await websocket.send_json({
                "type": "error",
                "message": e.message
            })
            await websocket.close()
            return

        # 5. 서비스 초기화
        streaming_stt = StreamingSTTService()
        similarity_service = SimilarityService()

        # 6. 오디오 버퍼 초기화
        buffer = AudioBuffer(chunk_duration=CHUNK_DURATION)
        full_transcript = ""

        print("[WebSocket] 준비 완료, 메시지 대기 중...")

        # 7. 메시지 루프
        while True:
            message = await websocket.receive_json()
            message_type = message.get("type")

            if message_type == "audio_chunk":
                # 오디오 청크 수신
                try:
                    audio_data = base64.b64decode(message["data"])
                    buffer.append(audio_data)

                    print(f"[WebSocket] 청크 수신: {len(audio_data)} bytes, "
                          f"누적: {buffer.duration:.2f}초")

                    # 1초치 누적되면 STT 처리
                    if buffer.is_ready:
                        wav_file = buffer.to_wav_file()
                        text = await streaming_stt.transcribe(wav_file)

                        full_transcript += " " + text

                        print(f"[WebSocket] STT 변환: {text}")

                        # 실시간 텍스트 전송
                        await websocket.send_json({
                            "type": "transcript",
                            "text": text
                        })

                        buffer.clear()

                except Exception as e:
                    print(f"[WebSocket] STT 처리 오류: {e}")
                    await websocket.send_json({
                        "type": "error",
                        "message": "음성 변환 중 오류가 발생했습니다"
                    })

            elif message_type == "end_stream":
                # 스트림 종료 - 최종 처리
                print("[WebSocket] 스트림 종료, 최종 처리 시작")

                try:
                    # 남은 버퍼 처리
                    if buffer.get_chunk_count() > 0:
                        print(f"[WebSocket] 남은 버퍼 처리: {buffer.duration:.2f}초")
                        wav_file = buffer.to_wav_file()
                        text = await streaming_stt.transcribe(wav_file)
                        full_transcript += " " + text

                    # 전체 텍스트 정리
                    full_transcript = full_transcript.strip()

                    print(f"[WebSocket] 전체 텍스트: {full_transcript}")
                    print(f"[WebSocket] 정답 텍스트: {answer_text}")

                    # 텍스트 정규화
                    normalized_user = streaming_stt.normalize_text(full_transcript)
                    normalized_expected = streaming_stt.normalize_text(answer_text)

                    # 유사도 계산
                    similarity = similarity_service.calculate_similarity(
                        normalized_user,
                        normalized_expected
                    )

                    is_correct = similarity >= SIMILARITY_THRESHOLD

                    print(f"[WebSocket] 유사도: {similarity:.4f}, 정답 여부: {is_correct}")

                    # S3 키 추출
                    audio_s3_key = message.get("audio_s3_key")

                    # DB 저장
                    saved_answer = await answer_repo.save_answer(
                        session_id=session_id,
                        seq_id=seq_id,
                        transcribed_text=full_transcript,
                        answer_text=answer_text,
                        similarity_score=float(similarity),
                        is_correct=is_correct,
                        audio_s3_key=audio_s3_key
                    )

                    print(f"[WebSocket] DB 저장 완료: answer_id={saved_answer.id}")

                    # 최종 결과 전송
                    await websocket.send_json({
                        "type": "final_result",
                        "session_stt_answer_id": saved_answer.id,
                        "transcribed_text": full_transcript,
                        "answer_text": answer_text,
                        "similarity_score": float(similarity),
                        "is_correct": is_correct,
                        "attempt_no": saved_answer.attempt_no
                    })

                    print("[WebSocket] 최종 결과 전송 완료")

                    # 루프 종료
                    break

                except Exception as e:
                    print(f"[WebSocket] 최종 처리 오류: {e}")
                    await websocket.send_json({
                        "type": "error",
                        "message": "결과 저장 중 오류가 발생했습니다"
                    })
                    break

            else:
                print(f"[WebSocket] 알 수 없는 메시지 타입: {message_type}")

    except WebSocketDisconnect:
        print(f"[WebSocket] 클라이언트 연결 끊김: session_id={session_id}")

    except Exception as e:
        print(f"[WebSocket] 예상치 못한 오류: {e}")
        try:
            await websocket.send_json({
                "type": "error",
                "message": "서버 오류가 발생했습니다"
            })
        except:
            pass

    finally:
        # 연결 종료
        try:
            await websocket.close()
            print(f"[WebSocket] 연결 종료: session_id={session_id}")
        except:
            pass
