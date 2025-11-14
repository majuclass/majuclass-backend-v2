"""
phonetic_utils: 발음 유사도 계산

한글 발음의 정확도를 평가하기 위한 유틸리티 함수들
- 자모 분해
- 음절 비교
- 편집 거리 계산
"""

from typing import List, Tuple


def phonetic_similarity(text1: str, text2: str) -> float:
    if not text1 or not text2:
        return 0.0

    # 자모 분해
    jamo1 = decompose_hangul(text1)
    jamo2 = decompose_hangul(text2)

    # 편집 거리 계산
    distance = edit_distance(jamo1, jamo2)

    # 최대 길이
    max_length = max(len(jamo1), len(jamo2))

    if max_length == 0:
        return 1.0

    # 유사도 = 1 - (거리 / 최대길이)
    similarity = 1.0 - (distance / max_length)

    return max(0.0, similarity)  # 음수 방지


def decompose_hangul(text: str) -> str:
    result = []

    for char in text:
        if _is_hangul(char):
            # 한글인 경우 자모 분해
            jamo = _decompose_char(char)
            result.extend(jamo)
        else:
            # 한글이 아니면 그대로 추가
            result.append(char)

    return ''.join(result)


def _decompose_char(char: str) -> List[str]:
    # 초성 리스트
    CHOSUNG = [
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    ]

    # 중성 리스트
    JUNGSUNG = [
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    ]

    # 종성 리스트 (첫 번째는 없음)
    JONGSUNG = [
        '', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    ]

    # 유니코드 값
    code = ord(char) - 0xAC00

    # 초성, 중성, 종성 인덱스 계산
    cho_idx = code // (21 * 28)
    jung_idx = (code % (21 * 28)) // 28
    jong_idx = code % 28

    # 자모 추출
    result = [CHOSUNG[cho_idx], JUNGSUNG[jung_idx]]

    # 종성이 있으면 추가
    if jong_idx > 0:
        result.append(JONGSUNG[jong_idx])

    return result


def _is_hangul(char: str) -> bool:
    code = ord(char)
    return 0xAC00 <= code <= 0xD7A3


def edit_distance(s1: str, s2: str) -> int:
    """
    레벤슈타인 편집 거리 (Levenshtein Distance)

    두 문자열 사이의 편집 거리를 계산합니다.
    삽입, 삭제, 교체 연산의 최소 횟수

    Args:
        s1: 첫 번째 문자열
        s2: 두 번째 문자열

    Returns:
        편집 거리 (작을수록 유사)

    Example:
        >>> edit_distance("ㅅㅏㄱㅗㅏ", "ㅆㅏㄱㅗㅏ")
        1  # 'ㅅ'을 'ㅆ'으로 교체 1회
    """
    len1, len2 = len(s1), len(s2)

    # DP 테이블 생성
    dp = [[0] * (len2 + 1) for _ in range(len1 + 1)]

    # 초기화
    for i in range(len1 + 1):
        dp[i][0] = i
    for j in range(len2 + 1):
        dp[0][j] = j

    # DP 계산
    for i in range(1, len1 + 1):
        for j in range(1, len2 + 1):
            if s1[i - 1] == s2[j - 1]:
                # 같으면 비용 0
                dp[i][j] = dp[i - 1][j - 1]
            else:
                # 다르면 삽입/삭제/교체 중 최소
                dp[i][j] = 1 + min(
                    dp[i - 1][j],      # 삭제
                    dp[i][j - 1],      # 삽입
                    dp[i - 1][j - 1]   # 교체
                )

    return dp[len1][len2]


def syllable_similarity(text1: str, text2: str) -> float:
    """
    음절 단위 유사도

    자모가 아닌 음절(글자) 단위로 비교합니다.

    Args:
        text1: 첫 번째 텍스트
        text2: 두 번째 텍스트

    Returns:
        유사도 (0~1)

    Example:
        >>> syllable_similarity("사과", "사과")
        1.0
        >>> syllable_similarity("사과", "싸과")
        0.5  # 2글자 중 1글자만 일치
    """
    if not text1 or not text2:
        return 0.0

    # 한글만 추출
    chars1 = [c for c in text1 if _is_hangul(c)]
    chars2 = [c for c in text2 if _is_hangul(c)]

    # 편집 거리
    distance = edit_distance(''.join(chars1), ''.join(chars2))

    # 최대 길이
    max_length = max(len(chars1), len(chars2))

    if max_length == 0:
        return 1.0

    # 유사도 계산
    similarity = 1.0 - (distance / max_length)

    return max(0.0, similarity)


def find_phonetic_errors(stt_text: str, answer_text: str) -> List[Tuple[str, str]]:
    """
    발음 오류 찾기

    STT 텍스트와 정답을 비교하여 발음 오류를 찾습니다.

    Args:
        stt_text: STT 변환 텍스트
        answer_text: 정답 텍스트

    Returns:
        [(잘못된 부분, 정답)] 리스트

    Example:
        >>> find_phonetic_errors("싸과", "사과")
        [("싸", "사")]
    """
    errors = []

    # 길이 맞추기
    min_len = min(len(stt_text), len(answer_text))

    for i in range(min_len):
        if stt_text[i] != answer_text[i]:
            # 한글인 경우만 오류로 간주
            if _is_hangul(stt_text[i]) and _is_hangul(answer_text[i]):
                errors.append((stt_text[i], answer_text[i]))

    return errors


def calculate_phonetic_accuracy(stt_text: str, answer_text: str) -> dict:
    jamo_sim = phonetic_similarity(stt_text, answer_text)
    syllable_sim = syllable_similarity(stt_text, answer_text)
    errors = find_phonetic_errors(stt_text, answer_text)

    return {
        "jamo_similarity": round(jamo_sim, 4),
        "syllable_similarity": round(syllable_sim, 4),
        "errors": errors,
        "error_count": len(errors)
    }

def quick_phonetic_score(stt_text: str, answer_text: str) -> float:
    return phonetic_similarity(stt_text, answer_text)
