/** @format */
import { useEffect, useRef } from "react";

type WebSocketManagerProps = {
  wsRef: React.MutableRefObject<WebSocket | null>;
  active: boolean;
  sessionId?: number;
  sequenceNumber?: number;
  onMessage?: (data: any) => void;
  onSendReady?: (
    send: (data: string) => void,
    sendEndStream: (audioS3Key: string, seq: number) => void
  ) => void;
};

export default function WebSocketManager({
  wsRef,
  active,
  sessionId,
  sequenceNumber,
  onMessage,
  onSendReady,
}: WebSocketManagerProps) {

  const internalRef = useRef<WebSocket | null>(null);
  const connectedRef = useRef(false);
  const token = localStorage.getItem("accessToken");

  useEffect(() => {
  if (!active || !sessionId || !sequenceNumber) return;
  if (!token) return;


    const wsUrl = `wss://www.majuclass.com/ws/stt/${sessionId}/${sequenceNumber}?token=${token}`;
    // const wsUrl = `ws://localhost:8000/ws/stt/${sessionId}/${sequenceNumber}`;

    const ws = new WebSocket(wsUrl);
    ws.binaryType = "arraybuffer";
    internalRef.current = ws;
    wsRef.current = ws; 
    connectedRef.current = true;

    // 연결 성공
    ws.onopen = () => {
      console.log(`WebSocket 연결됨 (session=${sessionId}, seq=${sequenceNumber})`);

      // 오디오 청크 전송
      const sendAudioChunk = (data: string) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: "audio_chunk", data }));
        } else {
          console.warn("WebSocket 닫힘 상태에서 audio_chunk 전송 시도");
        }
      };

      // 스트림 종료 전송
      const sendEndStream = (audioS3Key: string, seq: number) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(
            JSON.stringify({
              type: "end_stream",
              audio_s3_key: audioS3Key,
              sequence_number: seq,
            })
          );
        } else {
          console.warn("WebSocket 닫힘 상태에서 end_stream 전송 시도");
        }
      };

      // 상위 컴포넌트에 send 함수 전달
      onSendReady?.(sendAudioChunk, sendEndStream);
    };

    // 서버 메시지 수신
    ws.onmessage = (e) => {
      if (e.data instanceof ArrayBuffer) return;
      try {
        const parsed = JSON.parse(e.data);
        onMessage?.(parsed);
      } catch {
        console.warn("JSON 파싱 실패:", e.data);
      }
    };

    // 에러 감지
    ws.onerror = (err) => {
      console.error("WebSocket 오류 발생:", err);
    };

    // 연결 종료 감지
    ws.onclose = (e) => {
      console.warn(
        // `WebSocket 닫힘 (session=${sessionId}, seq=${sequenceNumber}) | code=${e.code} | reason=${e.reason || "없음"} | wasClean=${e.wasClean}`
      );
      if (e.code === 1006) {
        console.warn("서버 비정상 종료 — 재연결 또는 사용자 알림 필요");
      }
      connectedRef.current = false;
      if (wsRef.current === ws) wsRef.current = null;
    };

    // cleanup
    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close(1000, "Component unmounted");
        console.log("WebSocket 수동 종료 완료");
      }
      internalRef.current = null;
      wsRef.current = null;
      connectedRef.current = false;
    };
  }, [active, sessionId, sequenceNumber]);

  return null;
}
