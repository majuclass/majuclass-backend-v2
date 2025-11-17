/** @format */

import { useEffect, useRef, useState } from 'react';
import './AudioRecordingButton.css';

import Lottie from 'lottie-react';
import micRecording from '../../../assets/scenarios/animations/recording.json';
import startrecord from '../../../assets/scenarios/animations/start-record.json';
import api from '../../../apis/apiInstance';
import { fastapi } from '../../../apis/apiInstance';

export type STTResponse = {
  session_stt_answer_id: number;
  transcribed_text: string;
  answer_text: string;
  similarity_score: number;
  is_correct: boolean;
  attempt_no: number;
};

type Record = {
  sessionId: number;
  sequenceNumber: number;
  onSTTResult?: (result: STTResponse) => void;
};

export default function Record({
  sessionId,
  sequenceNumber,
  onSTTResult,
}: Record) {
  const [isRecording, setIsRecording] = useState(false);
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  const audioCtxRef = useRef<AudioContext | null>(null);
  const workletRef = useRef<AudioWorkletNode | null>(null);
  const pcmRef = useRef<Int16Array[]>([]);
  const streamRef = useRef<MediaStream | null>(null);

  const SAMPLE_RATE = 16000;

  // AudioWorklet 모듈
  useEffect(() => {
    const prepare = async () => {
      try {
        const ctx = new AudioContext({ sampleRate: SAMPLE_RATE });
        await ctx.audioWorklet.addModule('/pcm16-processor.js');
        // AudioWorklet만의 개별 Thread 생성 시점
        await ctx.close(); // 초기 등록만 하고 닫기
        setReady(true);
      } catch (err) {
        console.error('AudioWorklet 등록 실패:', err);
      }
    };
    prepare();
  }, []);

  // 녹음 시작
  const start = async () => {
    if (!ready) {
      alert('초기화 중입니다. 잠시 후 다시 시도하세요.');
      return;
    }

    // 이전 세션 정리
    await audioCtxRef.current?.close();
    audioCtxRef.current = null;
    workletRef.current = null;
    pcmRef.current = [];
    streamRef.current = null;

    // 새 AudioContext 생성
    const ctx = new AudioContext({ sampleRate: SAMPLE_RATE });
    await ctx.audioWorklet.addModule('/pcm16-processor.js');
    audioCtxRef.current = ctx;

    // 마이크 접근
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    streamRef.current = stream;
    const source = ctx.createMediaStreamSource(stream);

    // WorkletNode 생성
    const node = new AudioWorkletNode(ctx, 'pcm16-processor');
    workletRef.current = node;

    // PCM 데이터 수집
    node.port.onmessage = (audioMessage) => {
      const float32 = audioMessage.data as Float32Array;
      const pcm16 = new Int16Array(float32.length);
      const float32len = float32.length;
      for (let i = 0; i < float32len; i++) {
        const sample = Math.max(-1, Math.min(1, float32[i]));
        pcm16[i] =
          sample < 0
            ? Math.round(sample * 0x8000)
            : Math.round(sample * 0x7fff);
      }
      pcmRef.current.push(pcm16);
    };

    source.connect(node);
    setIsRecording(true);
  };

  // 녹음 중지
  const stop = async () => {
    const ctx = audioCtxRef.current;
    const node = workletRef.current;
    const stream = streamRef.current;
    if (!ctx || !node) return;

    // 연결 해제
    node.port.onmessage = null;
    node.disconnect();

    // 스트림 중지
    stream?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;

    // WAV 파일 변환
    const wav = makeWav(pcmRef.current, SAMPLE_RATE);
    const url = URL.createObjectURL(wav);
    setAudioUrl(url);
    setIsRecording(false);

    try {
      await uploadToS3(wav);
    } catch (err) {
      console.error('업로드 실패:', err);
    }

    // 정리
    pcmRef.current = [];
    workletRef.current = null;

    // AudioContext 닫기
    await ctx.close();
    audioCtxRef.current = null;
  };

  // WAV 인코딩
  const makeWav = (chunks: Int16Array[], sampleRate: number) => {
    const total = chunks.reduce((sum, c) => sum + c.length, 0);
    const buffer = new ArrayBuffer(44 + total * 2);
    const view = new DataView(buffer);

    const writeStr = (off: number, str: string) => {
      const strlen = str.length;
      for (let i = 0; i < strlen; i++)
        view.setUint8(off + i, str.charCodeAt(i));
    };

    let offset = 0;
    writeStr(offset, 'RIFF');
    offset += 4;
    view.setUint32(offset, 36 + total * 2, true);
    offset += 4;
    writeStr(offset, 'WAVE');
    offset += 4;
    writeStr(offset, 'fmt ');
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
    writeStr(offset, 'data');
    offset += 4;
    view.setUint32(offset, total * 2, true);
    offset += 4;

    let idx = 44;
    for (const chunk of chunks) {
      for (let i = 0; i < chunk.length; i++, idx += 2)
        view.setInt16(idx, chunk[i], true);
    }

    return new Blob([buffer], { type: 'audio/wav' });
  };

  // Presigned URL 요청
  const getPresignedUrl = async (): Promise<{ url: string; s3Key: string }> => {
    const token = localStorage.getItem('accessToken');

    const res = await api.post(
      '/scenario-sessions/audio-upload-url',
      {
        sessionId,
        sequenceNumber,
        contentType: 'audio/wav',
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    if (res.status !== 200) {
      throw new Error(`Presigned URL 발급 실패 (${res.status})`);
    }

    const presignedUrl = res.data?.data?.presignedUrl;
    const s3Key = res.data?.data?.s3Key;

    console.log('파일 경로:', s3Key);

    return { url: presignedUrl, s3Key };
  };

  // S3 업로드 함수
  const uploadToS3 = async (wavBlob: Blob) => {
    const { url, s3Key } = await getPresignedUrl();
    console.log('Presigned URL:', url);
    console.log(
      'Signed Headers:',
      url.includes('SignedHeaders=content-type') ? 'YES' : 'NO'
    );

    const res = await fetch(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'audio/wav',
      },
      body: wavBlob,
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(`S3 업로드 실패 (${res.status}): ${text}`);
    }

    console.log('S3 업로드 성공, STT 요청 시작...');
    const result = await requestSTTAnalyze(sessionId, sequenceNumber, s3Key);
    console.log('[STT RAW RESPONSE]', res);
    const sttData: STTResponse = result.data;

    if (onSTTResult) onSTTResult(sttData);
  };

  // STT 분석 요청 함수
  const requestSTTAnalyze = async (
    sessionId: number,
    seqNo: number,
    s3Key: string
  ) => {
    const token = localStorage.getItem('accessToken');

    try {
      const res = await fastapi.post(
        `/stt-analyze/${sessionId}/${seqNo}`,
        {
          audio_s3_key: s3Key,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      console.log('STT 분석 결과:', res.data);
      return res.data;
    } catch (err) {
      console.error('STT 분석 요청 실패:', err);
      throw err;
    }
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
