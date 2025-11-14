import re
from typing import List, Dict, Optional


def extract_keywords(text: str, language: str = "ko", min_length: int = 2) -> List[str]:
    if not text:
        return []

    # 간단한 방식: 공백으로 분리 + 필터링
    words = text.split()

    # 필터링: 최소 길이 이상, 한글 포함
    keywords = []
    for word in words:
        # 한글이 포함되어 있고, 최소 길이 이상
        if _contains_hangul(word) and len(word) >= min_length:
            keywords.append(word)

    # 중복 제거 (순서 유지)
    keywords = list(dict.fromkeys(keywords))

    return keywords


def extract_keywords_advanced(text: str, min_length: int = 2) -> List[str]:
    try:
        # KoNLPy 사용 시도
        from konlpy.tag import Okt
        okt = Okt()

        # 명사 추출
        nouns = okt.nouns(text)

        # 필터링: 최소 길이 이상
        keywords = [noun for noun in nouns if len(noun) >= min_length]

        # 중복 제거
        keywords = list(dict.fromkeys(keywords))

        return keywords

    except ImportError:
        # KoNLPy가 없으면 기본 방식 사용
        print("[keyword_utils] KoNLPy not found, using basic extraction")
        return extract_keywords(text, min_length=min_length)


def expand_synonyms(keywords: List[str], custom_synonyms: Optional[Dict[str, List[str]]] = None) -> List[str]:
    # 기본 동의어 사전
    default_synonyms = {
        # 과일
        "사과": ["애플", "apple"],
        "바나나": ["banana"],
        "포도": ["grape"],
        "딸기": ["strawberry"],
        "수박": ["watermelon"],

        # 색상
        "빨간색": ["빨강", "레드", "red", "빨강색", "빨간"],
        "파란색": ["파랑", "블루", "blue", "파랑색", "파란"],
        "노란색": ["노랑", "옐로우", "yellow", "노랑색", "노란"],
        "초록색": ["초록", "그린", "green", "초록빛", "녹색"],
        "검은색": ["검정", "블랙", "black", "검정색", "검은"],
        "하얀색": ["하양", "화이트", "white", "하양색", "하얀", "흰색"],

        # 학교 관련
        "학교": ["학원", "교실"],
        "선생님": ["교사", "teacher", "스승"],
        "학생": ["student", "제자"],

        # 일반 명사
        "집": ["home", "house", "가정"],
        "가족": ["family"],
        "친구": ["friend", "동무"],

        # 동사 (기본형)
        "먹다": ["먹는다", "먹어", "먹었다"],
        "가다": ["간다", "가", "갔다"],

    }

    # 사용자 정의 동의어 병합
    synonyms_dict = default_synonyms.copy()
    if custom_synonyms:
        synonyms_dict.update(custom_synonyms)

    # 확장된 키워드 저장
    expanded = set(keywords)  # 원본 키워드

    # 각 키워드에 대해 동의어 추가
    for keyword in keywords:
        if keyword in synonyms_dict:
            expanded.update(synonyms_dict[keyword])

    return list(expanded)


def get_keyword_variations(keyword: str) -> List[str]:
    variations = [keyword]

    # 조사 추가 변형
    particles = ["은", "는", "이", "가", "을", "를", "와", "과", "의", "에", "에서"]

    for particle in particles:
        variations.append(keyword + particle)

    return variations


def _contains_hangul(text: str) -> bool:
    hangul_pattern = re.compile('[ㄱ-ㅎ가-힣]+')
    return bool(hangul_pattern.search(text))


def calculate_keyword_coverage(
    stt_keywords: List[str],
    answer_keywords: List[str]
) -> float:
    if not answer_keywords:
        return 1.0  # 정답 키워드가 없으면 100%

    # 집합으로 변환
    stt_set = set(stt_keywords)
    answer_set = set(answer_keywords)

    # 교집합 계산
    matched = len(stt_set & answer_set)

    # 비율 계산
    coverage = matched / len(answer_set)

    return coverage


def find_missing_keywords(
    stt_keywords: List[str],
    answer_keywords: List[str]
) -> List[str]:
    stt_set = set(stt_keywords)
    answer_set = set(answer_keywords)

    missing = answer_set - stt_set

    return list(missing)


# 편의 함수
def quick_extract(text: str) -> List[str]:
    return extract_keywords(text, min_length=2)
