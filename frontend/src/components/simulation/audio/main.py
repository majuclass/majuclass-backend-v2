from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
import asyncio, base64, json, random
import pprint

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.websocket("/ws/stt/{session_id}/{sequence_number}")
async def websocket_stt(websocket: WebSocket, session_id: int, sequence_number: int):
    await websocket.accept()
    print(f"✅ WebSocket 연결됨: session={session_id}, seq={sequence_number}")

    chunk_count = 0
    try:
        while True:
            # 🎯 JSON 텍스트 메시지 수신
            message = await websocket.receive_text()
            data = json.loads(message)
            msg_type = data.get("type")
            pprint.pprint(f"\n[Raw Message from Front] {message}")
            # ✅ 오디오 청크 수신
            if msg_type == "audio_chunk":
                chunk_count += 1
                pcm_base64 = data.get("data")
                pcm_bytes = base64.b64decode(pcm_base64)   # 원래 PCM16 복원
                byte_size = len(pcm_bytes)
                # print(f"[{chunk_count:03d}] audio_chunk 수신 - {byte_size} bytes")

                # STT 처리 시뮬레이션 (0.05초 지연)
                await asyncio.sleep(0.05)
                fake_text = random.choice([
                    "안녕하세요", "좋아요", "테스트 중입니다", "네 알겠습니다", "감사합니다"
                ])

                await websocket.send_json({
                    "type": "partial_result",
                    "session_id": session_id,
                    "sequence_number": sequence_number,
                    "chunk_index": chunk_count,
                    "received_bytes": byte_size,
                    "partial_text": fake_text,
                    "message": f"Chunk {chunk_count} 처리 완료"
                })

            # ✅ 스트림 종료 처리
            elif msg_type in ["end_stream", "end"]:
                s3_key = data.get("audio_s3_key")
                pprint.pprint(f"스트림 종료 신호 수신 - S3 key: {s3_key}")

                await websocket.send_json({
                    "type": "final_result",
                    "session_id": session_id,
                    "sequence_number": sequence_number,
                    "message": f"총 {chunk_count}개 청크 수신 완료",
                    "audio_s3_key": s3_key,
                })
                break

            # ✅ 기타 메시지 처리
            else:
                print(f"⚪ 기타 메시지 수신: {data}")
                await websocket.send_json({
                    "type": "echo",
                    "message": data,
                })

    except WebSocketDisconnect:
        print(f"❌ 연결 종료: session={session_id}, seq={sequence_number}, 총 {chunk_count}개 청크 수신 완료")
