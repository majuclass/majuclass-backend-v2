"""
MySQL에서 시나리오 데이터를 추출하여 ChromaDB에 임베딩

사용법:
    python scripts/ingest_scenarios.py
"""
import asyncio
import os
import sys

# 프로젝트 루트를 Python path에 추가
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from sqlalchemy import text
from app.database import AsyncSessionLocal
from langchain_community.vectorstores import Chroma
from langchain_openai import OpenAIEmbeddings
from langchain.schema import Document
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv("../.env")


async def fetch_scenarios():
    """MySQL에서 시나리오 데이터 조회"""
    async with AsyncSessionLocal() as session:
        query = text("""
            SELECT
                s.id as scenario_id,
                s.title,
                s.summary,
                s.scenario_category_id as category_id,
                seq.seq_no,
                seq.question,
                GROUP_CONCAT(
                    CONCAT(o.option_text, '|', o.is_answer)
                    ORDER BY o.option_no
                    SEPARATOR ','
                ) as options
            FROM scenarios s
            LEFT JOIN scenario_sequences seq ON s.id = seq.scenario_id
            LEFT JOIN seq_options o ON seq.id = o.seq_id
            WHERE s.is_deleted = 0 AND seq.is_deleted = 0 AND o.is_deleted = 0
            GROUP BY s.id, seq.id
            ORDER BY s.id, seq.seq_no
        """)

        result = await session.execute(query)
        return result.fetchall()


def create_chunks(rows):
    """데이터를 LangChain Document로 변환"""
    documents = []
    scenario_cache = {}

    for row in rows:
        scenario_id = row.scenario_id

        # 청크 1: 시나리오 개요 (각 시나리오당 1번만)
        if scenario_id not in scenario_cache:
            overview_content = f"""제목: {row.title}
요약: {row.summary}
카테고리 ID: {row.category_id}"""

            documents.append(Document(
                page_content=overview_content,
                metadata={
                    "scenario_id": scenario_id,
                    "category_id": row.category_id,
                    "chunk_type": "overview"
                }
            ))
            scenario_cache[scenario_id] = True

        # 청크 2: 질문-답변 세트
        if row.question and row.options:
            options_list = row.options.split(',')
            correct = []
            wrong = []

            for opt in options_list:
                try:
                    text, is_ans = opt.rsplit('|', 1)
                    if is_ans == '1' or is_ans == 'True':
                        correct.append(text)
                    else:
                        wrong.append(text)
                except ValueError:
                    # 파싱 실패 시 스킵
                    continue

            if correct:  # 정답이 있는 경우에만 추가
                qa_content = f"""질문: {row.question}
정답: {', '.join(correct)}
오답 예시: {', '.join(wrong) if wrong else '없음'}"""

                documents.append(Document(
                    page_content=qa_content,
                    metadata={
                        "scenario_id": scenario_id,
                        "category_id": row.category_id,
                        "seq_no": row.seq_no,
                        "chunk_type": "qa"
                    }
                ))

    return documents


async def main():
    print("=" * 60)
    print("시나리오 데이터 → ChromaDB 임베딩 시작")
    print("=" * 60)

    # 1. MySQL 데이터 추출
    print("\n[1/4] MySQL 데이터 추출 중...")
    try:
        rows = await fetch_scenarios()
        print(f"✓ {len(rows)}개 레코드 추출 완료")
    except Exception as e:
        print(f"✗ MySQL 연결 실패: {e}")
        print("\n힌트: docs/.env 파일에서 DB 연결 정보를 확인하세요.")
        return

    if not rows:
        print("⚠ 시나리오 데이터가 없습니다.")
        return

    # 2. 청크 생성
    print("\n[2/4] Document 청크 생성 중...")
    documents = create_chunks(rows)
    print(f"✓ {len(documents)}개 청크 생성 완료")

    if not documents:
        print("⚠ 생성된 청크가 없습니다.")
        return

    # 3. OpenAI Embedding 초기화 (GMS API)
    print("\n[3/4] OpenAI Embedding 초기화 중...")
    try:
        embeddings = OpenAIEmbeddings(
            model="text-embedding-3-small",
            base_url=os.getenv("GMS_BASE"),
            api_key=os.getenv("GMS_KEY")
        )
        print("✓ Embedding 모델 준비 완료")
    except Exception as e:
        print(f"✗ Embedding 초기화 실패: {e}")
        print("\n힌트: docs/.env 파일에서 GMS_BASE, GMS_KEY를 확인하세요.")
        return

    # 4. ChromaDB 생성 및 임베딩
    print("\n[4/4] ChromaDB 임베딩 중 (시간 소요 예상)...")
    try:
        vectorstore = Chroma.from_documents(
            documents=documents,
            embedding=embeddings,
            persist_directory="./data/chroma_db",
            collection_name="scenario_kb"
        )
        print(f"✓ ChromaDB 저장 완료: ./data/chroma_db")
        print(f"✓ 총 {len(documents)}개 문서 임베딩 완료")
    except Exception as e:
        print(f"✗ ChromaDB 생성 실패: {e}")
        return

    # 5. 테스트 검색
    print("\n[테스트] 샘플 검색 수행...")
    try:
        test_query = "영화관에서 팝콘 주문하기"
        results = vectorstore.similarity_search_with_score(
            test_query, k=3
        )

        print(f"\n쿼리: '{test_query}'")
        for i, (doc, score) in enumerate(results, 1):
            print(f"\n결과 {i} (유사도: {score:.3f})")
            print(f"  {doc.page_content[:100]}...")
            print(f"  메타: {doc.metadata}")
    except Exception as e:
        print(f"⚠ 테스트 검색 실패: {e}")

    print("\n" + "=" * 60)
    print("완료!")
    print("=" * 60)
    print("\n다음 단계:")
    print("1. python scripts/test_rag.py 로 검색 테스트")
    print("2. API 서버에서 use_rag=true로 시나리오 생성")


if __name__ == "__main__":
    asyncio.run(main())
