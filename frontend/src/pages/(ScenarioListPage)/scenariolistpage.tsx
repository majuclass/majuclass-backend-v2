/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useMemo, useRef, useState } from "react";
import NavBar from "../(MainPage)/components/navbar"; // 네브바
import ScenarioCard from "./components/scencard";
import { fetchScenarios, type Difficulty, type Scenario } from "./api";

export default function ScenListPage() {
  const [items, setItems] = useState<Scenario[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 페이지네이션 상태
  const [page, setPage] = useState(0); // 0-based
  const pageSize = 12;
  const [hasNext, setHasNext] = useState(false);

  // 선택 필터(지금은 단순히 타입만 준비)
  const [categoryId] = useState<number | undefined>(undefined);
  const [difficulty] = useState<Difficulty | undefined>(undefined);

  const abortRef = useRef<AbortController | null>(null);

  // 시나리오 목록 로드
  useEffect(() => {
    setLoading(true);
    setError(null);

    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    fetchScenarios(
      { categoryId, difficulty, page, size: pageSize },
      controller.signal
    )
      .then((data) => {
        setItems(data);
        // 다음 페이지 존재 여부 추정
        setHasNext(data.length === pageSize);
      })
      .catch((e: any) => {
        if (e?.name === "AbortError") return;
        setError(e?.message ?? "시나리오 목록을 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, [categoryId, difficulty, page]);

  // 시뮬레이션 라우트 규약
  const toSimulation = useMemo(() => (id: number) => `/simulation/${id}`, []);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 상단 네브바 */}
      <NavBar />

      {/* 콘텐츠 */}
      <section className="px-6 py-8 max-w-7xl mx-auto">
        <h1 className="text-2xl font-semibold mb-6 text-gray-800">
          시나리오 목록
        </h1>

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
            <div
              className="
                grid gap-5
                [grid-template-columns:repeat(auto-fill,minmax(400px,1fr))]
              "
            >
              {items.map((sc) => (
                <ScenarioCard
                  key={sc.id}
                  scenario={sc}
                  onStartRoute={toSimulation}
                />
              ))}
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
    </div>
  );
}

/** 로딩 스켈레톤 */
function GridSkeleton() {
  const placeholders = Array.from({ length: 6 });
  return (
    <div
      className="
        grid gap-5
        [grid-template-columns:repeat(auto-fill,minmax(400px,1fr))]
      "
    >
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
