import re


def _normalize_whitespace(text: str) -> str:
    # 여러 공백 → 단일 공백
    text = re.sub(r'\s+', ' ', text)
    return text


def _normalize_repeated_chars(text: str, max_repeat: int = 2) -> str:
    # 동일 문자가 max_repeat번 이상 반복되면 max_repeat개로 축소
    pattern = r'(.)\1{' + str(max_repeat) + r',}'
    replacement = r'\1' * max_repeat
    text = re.sub(pattern, replacement, text)
    return text


class Normalizer:
    def __init__(self, aggressive: bool = False):
        self.aggressive = aggressive

    def normalize(self, text: str) -> str:
        if not text:
            return ""

        # 1. 앞뒤 공백 제거
        text = text.strip()

        # 2. 소문자 변환 (영문)
        text = text.lower()

        # 3. 특수문자 제거
        text = self._remove_special_chars(text)

        # 4. 반복 공백 제거
        text = _normalize_whitespace(text)

        # 5. 반복 문자 정규화 (선택)
        # "사과과과" → "사과" 같은 경우
        # text = self._normalize_repeated_chars(text)

        return text.strip()

    def _remove_special_chars(self, text: str) -> str:
        if self.aggressive:
            # 공격적 모드: 한글과 공백만 남김
            pattern = r'[^ㄱ-ㅎ가-힣\s]'
        else:
            # 일반 모드: 한글, 영문, 숫자, 공백 유지
            pattern = r'[^\w\sㄱ-ㅎ가-힣]'

        text = re.sub(pattern, '', text)
        return text

    def normalize_phonetic(self, text: str) -> str:
        # 기본 정규화 먼저 적용
        text = self.normalize(text)

        # 발음 유사 문자 매핑
        phonetic_map = {
            '애': '에',
            '얘': '예',
            '왜': '웨',
            '외': '웨',
            # 추가 매핑 규칙은 필요시 확장
        }

        for original, replacement in phonetic_map.items():
            text = text.replace(original, replacement)

        return text

    def remove_filler_words(self, text: str) -> str:

        # 기본 정규화 먼저
        text = self.normalize(text)

        # 추임새 목록
        filler_words = [
            r'\b음+\b',
            r'\b어+\b',
            r'\b그+\b',
            r'\b저+\b',
            r'\b뭐+\b',
        ]

        for filler in filler_words:
            text = re.sub(filler, '', text)

        # 정리
        text = _normalize_whitespace(text)
        return text.strip()


# 편의 함수
def normalize_text(text: str, aggressive: bool = False) -> str:
    normalizer = Normalizer(aggressive=aggressive)
    return normalizer.normalize(text)
