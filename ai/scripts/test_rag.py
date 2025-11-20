"""
RAG 검색 품질 테스트 스크립트

사용법:
    python scripts/test_rag.py
"""
import asyncio
import os
import sys

# 프로젝트 루트를 Python path에 추가
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.domains.scenario.services.rag_service import ScenarioRAGService
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv("docs/.env")


async def test_queries():
    """다양한 쿼리로 RAG 품질 테스트"""
    service = ScenarioRAGService()

    test_cases = [
        ("영화관에서 팝콘 주문하기", 1),
        ("병원에서 접수하기", 2),
        ("카페에서 음료 주문", 1),
        ("마트에서 장보기", 3),
        ("레스토랑에서 식사하기", 1),
    ]

    print("=" * 60)
    print("RAG 검색 품질 테스트")
    print("=" * 60)

    for query, category_id in test_cases:
        print(f"\n{'='*60}")
        print(f"쿼리: '{query}' (카테고리 {category_id})")
        print('='*60)

        try:
            results = await service.retrieve(query, category_id, top_k=3)

            if not results:
                print("❌ 검색 결과 없음")
                continue

            for i, r in enumerate(results, 1):
                print(f"\n결과 {i} (유사도: {r['score']:.3f})")
                print(f"타입: {r['metadata'].get('chunk_type')}")
                print(f"시나리오 ID: {r['metadata'].get('scenario_id')}")
                print(f"난이도: {r['metadata'].get('difficulty')}")
                print(f"내용:\n{r['content'][:200]}...")

        except Exception as e:
            print(f"❌ 검색 실패: {e}")

    print("\n" + "=" * 60)
    print("테스트 완료!")
    print("=" * 60)


async def test_single_query(prompt: str, category_id: int):
    """단일 쿼리 테스트"""
    service = ScenarioRAGService()

    print(f"\n검색 쿼리: '{prompt}'")
    print(f"카테고리: {category_id}\n")

    results = await service.retrieve(prompt, category_id, top_k=5)

    print(f"결과: {len(results)}개\n")

    for i, r in enumerate(results, 1):
        print(f"=== {i}. 유사도 {r['score']:.3f} ===")
        print(f"{r['content']}\n")
        print(f"메타데이터: {r['metadata']}\n")


if __name__ == "__main__":
    if len(sys.argv) > 2:
        # 커맨드 라인 인자로 테스트
        asyncio.run(test_single_query(sys.argv[1], int(sys.argv[2])))
    else:
        # 전체 테스트 케이스 실행
        asyncio.run(test_queries())
