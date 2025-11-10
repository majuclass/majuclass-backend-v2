import io
import wave
from typing import List


class AudioBuffer:
    def __init__(
        self,
        sample_rate: int = 16000,
        channels: int = 1,
        sample_width: int = 2,  # 16-bit = 2 bytes
        chunk_duration: float = 1.0
    ):
        self.sample_rate = sample_rate
        self.channels = channels
        self.sample_width = sample_width
        self.chunk_duration = chunk_duration
        self.chunks: List[bytes] = []

    def append(self, chunk: bytes):
        self.chunks.append(chunk)

    @property
    def duration(self) -> float:
        """
        현재 누적된 오디오의 총 시간 (초)

        Returns:
            시간 (초)

        계산식:
            total_bytes = 모든 청크의 바이트 합
            total_samples = total_bytes / sample_width
            duration = total_samples / sample_rate
        """
        if not self.chunks:
            return 0.0

        total_bytes = sum(len(chunk) for chunk in self.chunks)
        total_samples = total_bytes / (self.sample_width * self.channels)
        return total_samples / self.sample_rate

    @property
    def is_ready(self) -> bool:
        """
        처리 준비 완료 여부 (chunk_duration 이상 누적되었는지)

        Returns:
            True if duration >= chunk_duration
        """
        return self.duration >= self.chunk_duration

    def to_wav_file(self) -> io.BytesIO:
        """
        누적된 오디오를 WAV 파일로 변환

        Returns:
            BytesIO: 메모리 상의 WAV 파일

        사용 예시:
            wav_file = buffer.to_wav_file()
            # Whisper API에 전달
        """
        if not self.chunks:
            raise ValueError("버퍼가 비어있습니다")

        # 모든 청크 결합
        audio_data = b''.join(self.chunks)

        # WAV 파일 생성 (메모리)
        wav_buffer = io.BytesIO()

        with wave.open(wav_buffer, 'wb') as wav_file:
            wav_file.setnchannels(self.channels)
            wav_file.setsampwidth(self.sample_width)
            wav_file.setframerate(self.sample_rate)
            wav_file.writeframes(audio_data)

        # 파일 포인터를 처음으로 이동
        wav_buffer.seek(0)

        return wav_buffer

    def clear(self):
        """버퍼 초기화 (청크 삭제)"""
        self.chunks.clear()

    def get_total_bytes(self) -> int:
        """총 바이트 수"""
        return sum(len(chunk) for chunk in self.chunks)

    def get_chunk_count(self) -> int:
        """청크 개수"""
        return len(self.chunks)

    def __repr__(self) -> str:
        return (
            f"AudioBuffer("
            f"duration={self.duration:.2f}s, "
            f"chunks={self.get_chunk_count()}, "
            f"bytes={self.get_total_bytes()})"
        )
