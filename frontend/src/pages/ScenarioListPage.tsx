/**
 * eslint-disable @typescript-eslint/no-explicit-any
 *
 * @format
 */

import { useEffect, useMemo, useState } from 'react';
import ScenarioCard from '../components/scenariolistpage/ScenarioCard';
import {
  fetchScenarios,
  type Difficulty,
  type Scenario,
} from '../apis/scenariolistpageApi';
import { useNavigate } from 'react-router-dom';

import { getStudents } from '../apis/mainApi';
import SelectStudentModal from '../components/simulation/screen/SelectStudentModal';

export default function ScenarioListPage() {
  const [items, setItems] = useState<Scenario[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 페이지네이션 상태
  const [page, setPage] = useState(0); // 0-based
  const pageSize = 12;
  const [hasNext, setHasNext] = useState(false);

  // 선택 필터 (지금은 단순히 타입만 준비)
  const [categoryId] = useState<number | undefined>(undefined);
  const [difficulty] = useState<Difficulty | undefined>(undefined);

  const navigate = useNavigate();

  const [students, setStudents] = useState([]);

  const loadStudents = async () => {
    try {
      const data = await getStudents();  
      setStudents(data);
    } catch (error) {
      console.error("학생 목록 로드 실패:", error);
    }
  };


  // 시나리오 목록 로드
  useEffect(() => {
    let mounted = true;
    setLoading(true);
    setError(null);

    fetchScenarios({ categoryId, difficulty, page, size: pageSize })
      .then((data) => {
        if (!mounted) return;
        setItems(data);
        // axios + 현재 백엔드에선 page/size가 안 먹을 수도 있으니
        // 일단 길이로 다음 페이지 여부 추정
        setHasNext(data.length === pageSize);
      })
      .catch((e: Error) => {
        if (!mounted) return;
        setError(e?.message ?? '시나리오 목록을 불러오지 못했습니다.');
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [categoryId, difficulty, page]);
  
    useEffect(() => {
      loadStudents();
    }, []);

  // 시뮬레이션 라우트 규약
  const toSimulation = useMemo(() => (id: number) => `/simulation/${id}`, []);

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

        {/* 시나리오 추가 */}
        <div className="flex justify-end gap-4">
          <button
            className="w-full sm:w-auto p-3 font-nsrExtraBold mb-6 bg-gradient-to-r from-pink-400 to-pink-500 hover:from-pink-500 hover:to-pink-600 text-white rounded-full shadow-lg hover:shadow-lg transition-all duration-300 transform hover:scale-105"
            onClick={() => navigate('create')}
          >
            새 이야기 만들기
          </button>
          <button
            className="w-full sm:w-auto p-3 font-nsrExtraBold mb-6 bg-gradient-to-r from-purple-400 to-purple-500 hover:from-purple-500 hover:to-purple-600 text-white rounded-full shadow-lg hover:shadow-lg transition-all duration-300 transform hover:scale-105"
            onClick={() => navigate('/scenarios/ai/create')}
          >
            AI로 생성하기
          </button>
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

        {/* 목록 */}
        {!loading && !error && items.length > 0 && (
          <>
            <div className="bg-white rounded-3xl shadow-lg p-6 sm:p-8 mb-8">
              <div className="grid gap-4 sm:gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 justify-items-center py-6">
                {items.map((sc) => (
                  <ScenarioCard
                    key={sc.id}
                    scenario={sc}
                    onStartRoute={toSimulation}
                  />
                ))}
              </div>
            </div>

            {/* 페이지네이션 */}
            <div className="mt-8 flex items-center justify-center gap-2">
              <button
                className="h-9 px-3 rounded-md border border-gray-300 text-sm disabled:opacity-50"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                이전
              </button>

              <span className="text-sm text-gray-600">
                페이지 <b>{page + 1}</b>
              </span>

              <button
                className="h-9 px-3 rounded-md border border-gray-300 text-sm disabled:opacity-50"
                onClick={() => setPage((p) => p + 1)}
                disabled={!hasNext}
              >
                다음
              </button>
            </div>
          </>
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
