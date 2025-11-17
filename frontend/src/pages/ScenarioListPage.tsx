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
  const [page, setPage] = useState(0); // 0-based
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
    isLoading: categoriesLoading,
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
    <div className="min-h-screen bg-gray-50">
      {/* 콘텐츠 */}
      <section className="max-w-7xl mx-auto px-6 sm:px-6 lg:px-8 py-6 sm:py-8  text-base sm:text-lg">
        <div className="text-center mb-8">
          <div className="inline-block bg-white rounded-full px-8 py-4 shadow-lg">
            <h2 className="text-3xl font-bold font-nsrExtraBold">
              이야기 선택하기
            </h2>
          </div>
        </div>

        {/* 검색바와 액션 버튼 */}
        <div className="mb-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* 통합 검색바 (카테고리 필터 + 검색) */}
          <div className="relative flex-1 w-full max-w-xl">
            <div className="flex items-center border-2 border-gray-300 rounded-full shadow-sm focus-within:border-blue-500 transition-colors bg-white">
              {/* 카테고리 드롭다운 */}
              <select
                value={categoryId ?? ''}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : undefined)}
                className="px-4 py-3 text-base font-semibold text-gray-700 bg-transparent border-none outline-none cursor-pointer rounded-l-full appearance-none pr-8"
                style={{
                  backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236b7280'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E")`,
                  backgroundRepeat: 'no-repeat',
                  backgroundPosition: 'right 0.5rem center',
                  backgroundSize: '1.25rem'
                }}
              >
                <option value="">전체</option>
                {categoryList.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.categoryName}
                  </option>
                ))}
              </select>

              {/* 구분선 */}
              <div className="h-8 w-px bg-gray-300"></div>

              {/* 검색 입력 */}
              <input
                type="text"
                placeholder="시나리오 제목으로 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 px-4 py-3 text-base border-none outline-none rounded-r-full"
              />

              {/* 검색 아이콘 */}
              <svg
                className="absolute right-4 w-5 h-5 text-gray-400 pointer-events-none"
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

          {/* 시나리오 생성 버튼 */}
          <div className="flex gap-3 w-full sm:w-auto">
            <button
              className="flex-1 sm:flex-none px-5 py-3 font-nsrExtraBold bg-gradient-to-r from-pink-400 to-pink-500 hover:from-pink-500 hover:to-pink-600 text-white rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
              onClick={() => navigate('create')}
            >
              새 이야기 만들기
            </button>
            <button
              className="flex-1 sm:flex-none px-5 py-3 font-nsrExtraBold bg-gradient-to-r from-purple-400 to-purple-500 hover:from-purple-500 hover:to-purple-600 text-white rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
              onClick={() => navigate('/scenarios/ai/create')}
            >
              AI로 생성하기
            </button>
          </div>
        </div>

        {/* 로딩 */}
        {loading && <GridSkeleton />}

        {/* 에러 */}
        {!loading && error && (
          <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* 결과 없음 */}
        {!loading && !error && items.length === 0 && (
          <div className="rounded-lg border border-gray-200 bg-white p-8 text-center text-sm text-gray-600">
            표시할 시나리오가 없습니다.
          </div>
        )}

        {/* 검색/필터 결과 없음 */}
        {!loading && !error && items.length > 0 && filteredItems.length === 0 && (
          <div className="rounded-lg border border-gray-200 bg-white p-8 text-center text-sm text-gray-600">
            검색 결과가 없습니다. 다른 검색어를 입력해보세요.
          </div>
        )}

        {/* 목록 */}
        {!loading && !error && filteredItems.length > 0 && (
          <div className="bg-white rounded-3xl shadow-lg p-6 sm:p-8 mb-8">
            <div className="grid gap-4 sm:gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 justify-items-center py-6">
              {filteredItems.map((sc) => (
                <ScenarioCard
                  key={sc.id}
                  scenario={sc}
                  onStartRoute={toSimulation}
                />
              ))}
            </div>

            {/* 무한 스크롤 감지 요소 */}
            {hasMore && <div ref={sentinelRef} className="h-4" />}

            {/* 추가 로딩 인디케이터 */}
            {loadingMore && (
              <div className="py-8 text-center">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-200 border-t-blue-500"></div>
                <p className="mt-2 text-sm text-gray-600">더 많은 시나리오를 불러오는 중...</p>
              </div>
            )}

            {/* 더 이상 데이터 없음 */}
            {!hasMore && items.length > 0 && (
              <div className="py-6 text-center text-sm text-gray-500">
                모든 시나리오를 불러왔습니다.
              </div>
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
    <div className="grid gap-5 grid-cols-3 justify-items-center">
      {placeholders.map((_, i) => (
        <div
          key={i}
          className="w-[400px] h-[240px] rounded-2xl border border-gray-100 bg-white p-0 shadow-sm"
        >
          <div className="h-[144px] animate-pulse bg-gray-100" />
          <div className="px-4">
            <div className="my-3 h-4 w-2/3 animate-pulse rounded bg-gray-100" />
          </div>
          <div className="flex items-center gap-2 px-4">
            <div className="h-9 w-full animate-pulse rounded-md bg-gray-100" />
            <div className="h-9 w-full animate-pulse rounded-md bg-gray-100" />
          </div>
        </div>
      ))}
    </div>
  );
}
