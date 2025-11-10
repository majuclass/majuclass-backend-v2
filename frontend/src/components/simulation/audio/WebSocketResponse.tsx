/** @format */
import { useEffect, useState } from "react";

type WebSocketResponseProps = {
  messageData?: any; // WebSocket에서 받은 JSON 데이터
};

export default function WebSocketResponse({ messageData }: WebSocketResponseProps) {
  const [partialText, setPartialText] = useState<string>("");
  const [finalMessage, setFinalMessage] = useState<string>("");

  useEffect(() => {
    if (!messageData) return;

    // 서버에서 받은 데이터 타입에 따라 처리
    if (messageData.type === "partial_result") {
      setPartialText(messageData.partial_text);
    } else if (messageData.type === "final_result") {
      setFinalMessage(messageData.message);
      setPartialText(""); // 최종 결과 오면 임시 텍스트 초기화
    } else if (typeof messageData === "string") {
      setPartialText(messageData); // 일반 텍스트 수신 시
    }
  }, [messageData]);

  return (
    <div className="p-4 rounded-xl bg-white/70 shadow-md text-center mt-4 w-[300px]">
      {partialText && (
        <div className="text-lg text-gray-700 font-semibold">
          실시간 인식: <span className="text-blue-600">{partialText}</span>
        </div>
      )}
      {finalMessage && (
        <div className="mt-2 text-green-700 font-bold">
          최종 결과: {finalMessage}
        </div>
      )}
    </div>
  );
}
