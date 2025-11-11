class PCM16Processor extends AudioWorkletProcessor {
  constructor() {
    super();
    this.buffer = [];
    this.sampleRate = sampleRate; 
    this.chunkSamples = Math.floor(this.sampleRate * 0.25); // 0.5초 분량 샘플 개수
  }

  process(inputs) {
    const input = inputs[0];
    if (!input || !input[0]) return true;

    const float32 = input[0];
    this.buffer.push(...float32);

    if (this.buffer.length >= this.chunkSamples) {
      const chunk = this.buffer.slice(0, this.chunkSamples);
      this.buffer = this.buffer.slice(this.chunkSamples);

      // Float32Array → ArrayBuffer로 변환 후 전송
      const float32array = new Float32Array(chunk);
      const buffer = float32array.buffer;
      this.port.postMessage(buffer, [buffer]);
    }

    return true;
  }
}

registerProcessor("pcm16-processor", PCM16Processor);

