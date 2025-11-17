/**
 * eslint-disable @typescript-eslint/no-explicit-any
 *
 * @format
 */

import { useEffect, useMemo, useRef, useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import ScenarioCard from '../components/scenariolistpage/ScenarioCard';
import {
  fetchScenarios,
  type Difficulty,
  type Scenario,
} from '../apis/scenariolistpageApi';
import { useNavigate } from 'react-router-dom';

import { getStudents } from '../apis/mainApi';
import SelectStudentModal from '../components/simulation/screen/SelectStudentModal';
import api from '../apis/apiInstance';
import "../styles/ScenarioList.css"

type Category = {
  id: number;
  categoryName: string;
};

// 카테고리 조회 API
const fetchCategories = async () => {
  const resp = await api.get('categories');
  return resp.data.data as Category[];
};

export default function ScenarioListPage() {
  const [items, setItems] = useState<Scenario[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 무한 스크롤 상태
  const [, setPage] = useState(0); // 0-based
  const pageSize = 12;
  const [hasMore, setHasMore] = useState(true);
  const [isInitialLoad, setIsInitialLoad] = useState(true); // 초기 로드 플래그

  // 검색 및 필터 상태
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [difficulty] = useState<Difficulty | undefined>(undefined);

  const navigate = useNavigate();

  const [students, setStudents] = useState([]);

  // Intersection Observer용 ref
  const observerRef = useRef<IntersectionObserver | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  // 카테고리 조회
  const {
    data: categories,
    // isLoading: categoriesLoading,
  } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 1000 * 60 * 60 * 24, // 24시간
  });

  const categoryList = categories || [];

  const loadStudents = async () => {
    try {
      const data = await getStudents();
      setStudents(data);
    } catch (error) {
      console.error("학생 목록 로드 실패:", error);
    }
  };

  // 시나리오 목록 로드
  const loadScenarios = useCallback(async (pageNum: number, isInitial = false) => {
    if (isInitial) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }
    setError(null);

    try {
      const data = await fetchScenarios({ categoryId, difficulty, page: pageNum, size: pageSize });

      if (isInitial) {
        setItems(data);
      } else {
        setItems((prev) => [...prev, ...data]);
      }

      // 다음 페이지가 있는지 확인
      setHasMore(data.length === pageSize);
    } catch (e) {
      const error = e as Error;
      setError(error?.message ?? '시나리오 목록을 불러오지 못했습니다.');
    } finally {
      if (isInitial) {
        setLoading(false);
        // 초기 로드 완료 후 약간의 딜레이를 두고 무한 스크롤 활성화
        setTimeout(() => setIsInitialLoad(false), 100);
      } else {
        setLoadingMore(false);
      }
    }
  }, [categoryId, difficulty]);

  // 초기 로드 및 필터 변경 시 리셋
  useEffect(() => {
    setPage(0);
    setHasMore(true);
    setIsInitialLoad(true); // 초기 로드 플래그 리셋
    loadScenarios(0, true);
  }, [categoryId, difficulty]);

  // 무한 스크롤 Intersection Observer 설정
  useEffect(() => {
    // 초기 로드 중이거나 로딩 중이거나 더 이상 데이터가 없으면 리턴
    if (isInitialLoad || loading || !hasMore) return;

    observerRef.current = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry.isIntersecting && !loadingMore && hasMore) {
          setPage((prev) => {
            const nextPage = prev + 1;
            loadScenarios(nextPage);
            return nextPage;
          });
        }
      },
      { threshold: 0.1 }
    );

    if (sentinelRef.current) {
      observerRef.current.observe(sentinelRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [isInitialLoad, loading, loadingMore, hasMore, loadScenarios]);

  useEffect(() => {
    loadStudents();
  }, []);

  // 시뮬레이션 라우트 규약
  const toSimulation = useMemo(() => (id: number) => `/simulation/${id}`, []);

  // 검색어 및 카테고리 필터 적용된 아이템
  const filteredItems = useMemo(() => {
    let result = [...items];

    // 제목으로 검색
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      result = result.filter((item) =>
        item.title.toLowerCase().includes(query)
      );
    }

    return result;
  }, [items, searchQuery]);

     return (
    <div className="page-container">
  <section className="section-wrapper">
    <div className="title-area">
      <div className="title-bubble">
        <h2 className="title-text"><span>이야기</span> 선택하기</h2>
      </div>
    </div>

    {/* 검색바 + 버튼 */}
    <div className="search-actions-wrapper">

      {/* 검색바 */}
      <div className="searchbar-container">
        <div className="searchbar-box">
          
          {/* 카테고리 select */}
          <select
            value={categoryId ?? ""}
            onChange={(e) =>
              setCategoryId(
                e.target.value ? Number(e.target.value) : undefined
              )
            }
            className="category-select"
           
          >
            <option value="">전체</option>
            {categoryList.map((category) => (
              <option key={category.id} value={category.id}>
                {category.categoryName}
              </option>
            ))}
          </select>

          {/* 구분선 */}
          <div className="vert-divider"></div>

          {/* 검색 입력 */}
          <input
            type="text"
            placeholder="시나리오 제목으로 검색..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />

          {/* 돋보기 아이콘 */}
          <svg
            className="search-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>

        </div>
      </div>

      {/* 버튼 */}
      <div className="action-buttons">
        <button
          className="action-btn btn-pink"
          onClick={() => navigate("create")}
        >
          새 이야기 만들기
        </button>

        <button
          className="action-btn btn-purple"
          onClick={() => navigate("/scenarios/ai/create")}
        >
          AI로 생성하기
        </button>
      </div>

    </div>

    {/* 로딩 */}
    {loading && <GridSkeleton />}

    {/* 에러 */}
    {!loading && error && <div className="error-box">{error}</div>}

    {/* 결과 없음 */}
    {!loading && !error && items.length === 0 && (
      <div className="empty-box">표시할 시나리오가 없습니다.</div>
    )}

    {/* 검색결과 없음 */}
    {!loading && !error && items.length > 0 && filteredItems.length === 0 && (
      <div className="empty-box">
        검색 결과가 없습니다. 다른 검색어를 입력해보세요.
      </div>
    )}

    {/* 목록 */}
    {!loading && !error && filteredItems.length > 0 && (
      <div className="card-list-wrapper">
        <div className="card-grid">
          {filteredItems.map((sc) => (
            <ScenarioCard
              key={sc.id}
              scenario={sc}
              onStartRoute={toSimulation}
            />
          ))}
        </div>

        {/* sentinel */}
        {hasMore && <div ref={sentinelRef} className="sentinel" />}

        {/* 추가 로딩 */}
        {loadingMore && (
          <div className="loading-more">
            <div className="spinner"></div>
            <p className="loading-text">더 많은 시나리오를 불러오는 중...</p>
          </div>
        )}

        {/* 끝 */}
        {!hasMore && items.length > 0 && (
          <div className="loading-text">모든 시나리오를 불러왔습니다.</div>
        )}
      </div>
    )}
  </section>

  <SelectStudentModal students={students} />
</div>

  );
}


/** 로딩 스켈레톤 */
function GridSkeleton() {
  const placeholders = Array.from({ length: 6 });
    return (
    <div className="skeleton-grid">
      {placeholders.map((_, i) => (
        <div key={i} className="skeleton-card">
          <div className="skeleton-top" />

          <div className="skeleton-title-wrapper">
            <div className="skeleton-title" />
          </div>

          <div className="skeleton-buttons">
            <div className="skeleton-btn" />
            <div className="skeleton-btn" />
          </div>
        </div>
      ))}
    </div>
  );
}