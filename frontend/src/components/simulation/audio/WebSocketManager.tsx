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
    console.log(
      `useEffect 트리거됨 | active=${active} | sessionId=${sessionId} | seq=${sequenceNumber} | connected=${connectedRef.current}`
    );

    if (!active || !sessionId || !sequenceNumber) {
      console.log("WebSocket 연결 생략 (조건 불충족)");
      return;
    }

    if (!token) {
      console.warn("accessToken이 없습니다. WebSocket 연결 중단.");
      return;
    }

    if (connectedRef.current) {
      console.log("이미 연결되어 있음, 새로 생성하지 않음");
      return;
    }

    // URL 인코딩된 토큰 사용
    // const encodedToken = encodeURIComponent(token);
    const wsUrl = `wss://k13a202.p.ssafy.io/ws/stt/${sessionId}/${sequenceNumber}?token=${token}`;
    // const wsUrl = `wss://k13a202.p.ssafy.io/ws/stt/1/${sequenceNumber}?token=${token}`;
    console.log(`WebSocket 연결 시도: ${wsUrl}`);

    const ws = new WebSocket(wsUrl);
    ws.binaryType = "arraybuffer";
    internalRef.current = ws;
    wsRef.current = ws;
    connectedRef.current = true;

    // 연결 성공
    ws.onopen = () => {
      console.log(`WebSocket 연결됨 (session=${sessionId}, seq=${sequenceNumber})`);

      // 🎙 오디오 청크 전송 (JSON 구조 필수)
      const sendAudioChunk = (data: string) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: "audio_chunk", data }));
          console.log(`audio_chunk 전송 (${data.length} bytes)`);
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
          console.log(`end_stream 전송 (seq=${seq}, key=${audioS3Key})`);
        } else {
          console.warn("WebSocket 닫힘 상태에서 end_stream 전송 시도");
        }
      };

      // 상위 컴포넌트에 send 함수 전달
      onSendReady?.(sendAudioChunk, sendEndStream);
      console.log("onSendReady 전달 완료");
    };

    // 서버 메시지 수신
    ws.onmessage = (e) => {
      if (e.data instanceof ArrayBuffer) return;
      try {
        const parsed = JSON.parse(e.data);
        console.log("서버 → 클라 메시지 수신:", parsed);
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
        `🔌 WebSocket 닫힘 (session=${sessionId}, seq=${sequenceNumber}) | code=${e.code} | reason=${e.reason || "없음"} | wasClean=${e.wasClean}`
      );
      if (e.code === 1006) {
        console.warn("서버 비정상 종료 — 재연결 또는 사용자 알림 필요");
      }
      connectedRef.current = false;
      if (wsRef.current === ws) wsRef.current = null;
    };

    // cleanup
    return () => {
      console.log("🧹 cleanup 실행됨 (WebSocketManager unmount or deps change)");
      if (ws.readyState === WebSocket.OPEN) {
        ws.close(1000, "Component unmounted");
        console.log("🧹 WebSocket 수동 종료 완료");
      }
      internalRef.current = null;
      wsRef.current = null;
      connectedRef.current = false;
    };
  }, [active, sessionId, sequenceNumber]);

  return null;
}
