"""
STTPort: Speech-to-Text 인터페이스

음성 인식 시스템과의 통신을 추상화합니다.
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass


@dataclass
class STTResult:
    """STT 변환 결과"""
    text: str           # 변환된 텍스트
    confidence: float   # 신뢰도 (0~1)
    duration: float     # 오디오 길이 (초)


class STTPort(ABC):
    """음성 인식 추상 인터페이스"""

    @abstractmethod
    async def transcribe(self, audio_data: bytes, language: str = "ko") -> STTResult:
        """
        음성을 텍스트로 변환

        Args:
            audio_data: 오디오 파일 데이터 (바이트)
            language: 언어 코드 (기본값: "ko")

        Returns:
            STTResult: 변환 결과

        Raises:
            Exception: STT 변환 실패 시
        """
        pass
