/** @format */

import { useEffect, useRef, useState } from "react";
import "./AudioRecordingButton.css";
import Lottie from "lottie-react";
import micRecording from "../../../assets/scenarios/animations/recording.json";
import startrecord from "../../../assets/scenarios/animations/start-record.json";


type AudioRecorderProps = {
  sessionId: number;
  sequenceNumber: number;
  sendPCMChunk?: (data: string) => void;
  sendEndStream?: (audioS3Key: string, seq: number) => void;
};

export default function AudioRecorder({
  sessionId,
  sequenceNumber,
  sendPCMChunk,
  sendEndStream,
}: AudioRecorderProps) {
  const [isRecording, setIsRecording] = useState(false);
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  const audioCtxRef = useRef<AudioContext | null>(null);
  const workletRef = useRef<AudioWorkletNode | null>(null);
  const pcmRef = useRef<Int16Array[]>([]);
  const streamRef = useRef<MediaStream | null>(null);
  const SAMPLE_RATE = 16000;
  const baseUrl = import.meta.env.VITE_BASE_API_URL;

  // AudioWorklet 준비
  useEffect(() => {
    const prepare = async () => {
      try {
        const ctx = new AudioContext({ sampleRate: SAMPLE_RATE });
        await ctx.audioWorklet.addModule("/pcm16-processor.js");
        await ctx.close();
        setReady(true);
      } catch (err) {
        console.error("AudioWorklet 등록 실패:", err);
      }
    };
    prepare();
  }, []);

  // 녹음 시작
  const start = async () => {
    if (!ready) {
      alert("초기화 중입니다. 잠시 후 다시 시도하세요.");
      return;
    }

    await audioCtxRef.current?.close();
    audioCtxRef.current = null;
    workletRef.current = null;
    pcmRef.current = [];
    streamRef.current = null;

    const ctx = new AudioContext({ sampleRate: SAMPLE_RATE });
    await ctx.audioWorklet.addModule("/pcm16-processor.js");
    audioCtxRef.current = ctx;

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    streamRef.current = stream;
    const source = ctx.createMediaStreamSource(stream);

    const node = new AudioWorkletNode(ctx, "pcm16-processor");
    workletRef.current = node;

    // let chunkCount = 0;
    node.port.onmessage = (audioMessage) => {
      const buffer = audioMessage.data as ArrayBuffer;
      const float32 = new Float32Array(buffer);

      const pcm16 = new Int16Array(float32.length);
      for (let i = 0; i < float32.length; i++) {
        const s = Math.max(-1, Math.min(1, float32[i]));
        pcm16[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
      }
      pcmRef.current.push(pcm16);

      const uint8 = new Uint8Array(pcm16.buffer);
      let binary = "";
      for (let i = 0; i < uint8.length; i++)
        binary += String.fromCharCode(uint8[i]);
      const base64 = btoa(binary);

      //   chunkCount++;
      if (sendPCMChunk) sendPCMChunk(base64);
      else console.warn("sendPCMChunk 없음 → 청크 전송 불가!");
    };

    source.connect(node);
    setIsRecording(true);
  };

  // 녹음 중지
  const stop = async () => {
    const ctx = audioCtxRef.current;
    const node = workletRef.current;
    const stream = streamRef.current;
    if (!ctx || !node) {
      console.warn("stop() 시점에 AudioContext 또는 Worklet 없음");
      return;
    }

    node.port.onmessage = null;
    node.disconnect();

    stream?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;

    const wav = makeWav(pcmRef.current, SAMPLE_RATE);
    const url = URL.createObjectURL(wav);
    setAudioUrl(url);
    setIsRecording(false);

    try {
      const s3Key = await uploadToS3(wav);
      console.log(`업로드 성공: ${s3Key}`);
      if (sendEndStream) {
        console.log(`end_stream 전송 (seq=${sequenceNumber})`);
        sendEndStream(s3Key, sequenceNumber);
      } else {
        console.warn("sendEndStream 없음 → end_stream 전송 불가");
      }
    } catch (err) {
      console.error("업로드 실패:", err);
    }

    pcmRef.current = [];
    workletRef.current = null;

    await ctx.close();
    audioCtxRef.current = null;
  };

  // WAV 인코딩
  const makeWav = (chunks: Int16Array[], sampleRate: number) => {
    const total = chunks.reduce((sum, c) => sum + c.length, 0);
    const buffer = new ArrayBuffer(44 + total * 2);
    const view = new DataView(buffer);

    const writeStr = (off: number, str: string) => {
      for (let i = 0; i < str.length; i++)
        view.setUint8(off + i, str.charCodeAt(i));
    };

    let offset = 0;
    writeStr(offset, "RIFF");
    offset += 4;
    view.setUint32(offset, 36 + total * 2, true);
    offset += 4;
    writeStr(offset, "WAVE");
    offset += 4;
    writeStr(offset, "fmt ");
    offset += 4;
    view.setUint32(offset, 16, true);
    offset += 4;
    view.setUint16(offset, 1, true);
    offset += 2;
    view.setUint16(offset, 1, true);
    offset += 2;
    view.setUint32(offset, sampleRate, true);
    offset += 4;
    view.setUint32(offset, sampleRate * 2, true);
    offset += 4;
    view.setUint16(offset, 2, true);
    offset += 2;
    view.setUint16(offset, 16, true);
    offset += 2;
    writeStr(offset, "data");
    offset += 4;
    view.setUint32(offset, total * 2, true);
    offset += 4;

    let idx = 44;
    for (const chunk of chunks) {
      for (let i = 0; i < chunk.length; i++, idx += 2)
        view.setInt16(idx, chunk[i], true);
    }

    return new Blob([buffer], { type: "audio/wav" });
  };

  const getPresignedUrl = async (): Promise<{ url: string; s3Key: string }> => {
    const token = localStorage.getItem("accessToken");
    const res = await fetch(
      `${baseUrl}/scenario-sessions/audio-upload-url`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          sessionId,
          sequenceNumber,
          contentType: "audio/wav",
        }),
      }
    );

    if (!res.ok) throw new Error(`Presigned URL 발급 실패 (${res.status})`);
    const json = await res.json();
    const presignedUrl = json?.data?.presignedUrl;
    const s3Key = json?.data?.s3Key;
    return { url: presignedUrl, s3Key };
  };

  const uploadToS3 = async (wavBlob: Blob): Promise<string> => {
    const { url, s3Key } = await getPresignedUrl();
    const res = await fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "audio/wav" },
      body: wavBlob,
    });

    if (!res.ok) throw new Error(`S3 업로드 실패 (${res.status})`);
    return s3Key;
  };

  return (
    <div className="wrap">
      {!isRecording ? (
        <button onClick={start} disabled={!ready} className="record-start">
          <Lottie animationData={startrecord} loop={true} />
        </button>
      ) : (
        <div className="recording-lottie" onClick={stop}>
          <Lottie animationData={micRecording} loop={true} />
        </div>
      )}

      {audioUrl && (
        <div className="audio-play">
          <audio controls src={audioUrl} />
        </div>
      )}
    </div>
  );
}
