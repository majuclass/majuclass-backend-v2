// src/hooks/useTTS.ts
import api from "../../../apis/apiInstance";

let globalRequestId = 0;

export const useTTS = () => {
  const play = async (text: string) => {
    try {
      const requestId = ++globalRequestId; 

      const token = localStorage.getItem("accessToken");

      const res = await api.get("/tts", {
        baseURL: import.meta.env.VITE_BASE_AI_URL,
        headers: { Authorization: `Bearer ${token}` },
        params: { text },
        responseType: "blob",
      });

      if (requestId !== globalRequestId) {
        return;
      }

      const audioBlob = res.data;
      const url = URL.createObjectURL(audioBlob);

      const audio = new Audio(url);
      audio.play();

      audio.onended = () => {
        URL.revokeObjectURL(url);
      };
    } catch (err) {
      console.error("TTS 요청 실패:", err);
    }
  };

  return { play };
};
