// class PCM16Processor extends AudioWorkletProcessor {
//   process(inputs) {
//     const input = inputs[0][0];
//     if (input) this.port.postMessage(input);
//     return true;
//   }
// }
// registerProcessor("pcm16-processor", PCM16Processor);
// public/pcm16-processor.js
class PCM16Processor extends AudioWorkletProcessor {
  process(inputs) {
    const input = inputs[0];
    if (input && input[0]) {
      // Float32Array 복사본을 만들어서 buffer만 전송 (transferable object)
      const float32 = input[0];
      const buffer = float32.slice().buffer;
      this.port.postMessage(buffer, [buffer]);
    }
    return true;
  }
}

registerProcessor("pcm16-processor", PCM16Processor);
