/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Scenario } from "../api";
import { fetchScenarioById } from "../api";

// 모듈 스코프 간단 캐시 (플립해서 상세 본 카드 재요청 방지)
const scenarioDetailCache = new Map<number, Scenario>();

type Props = {
  scenario: Scenario; // 목록 API에서 온 얕은 정보
  onStartRoute?: (id: number) => string; // 라우팅 커스터마이즈용 (선택)
  className?: string;
};

export default function ScenarioCard({ scenario, onStartRoute, className }: Props) {
  const navigate = useNavigate();
  const [isFlipped, setIsFlipped] = useState(false);
  const [detail, setDetail] = useState<Scenario>(scenario);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [errorDetail, setErrorDetail] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);
  const frontBtnRef = useRef<HTMLButtonElement | null>(null);
  const backFirstFocusRef = useRef<HTMLButtonElement | null>(null);

  const startHref = useMemo(
    () => (onStartRoute ? onStartRoute(detail.id) : `/simulation/${detail.id}`),
    [detail.id, onStartRoute]
  );

  // 이미지 에러 핸들링
  const [thumbError, setThumbError] = useState(false);
  useEffect(() => {
    // detail 변경 시 에러 상태 초기화
    setThumbError(false);
  }, [detail.thumbnailUrl, detail.id]);

  // 플립 토글
  const openBack = async () => {
    setIsFlipped(true);

    // 상세가 캐시에 있으면 즉시 반영
    const cached = scenarioDetailCache.get(scenario.id);
    if (cached) {
      setDetail(cached);
      queueMicrotask(() => backFirstFocusRef.current?.focus());
      return;
    }

    // 최초 상세 로드
    setLoadingDetail(true);
    setErrorDetail(null);
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    try {
      const full = await fetchScenarioById(scenario.id, controller.signal);
      scenarioDetailCache.set(scenario.id, full);
      setDetail(full);
      queueMicrotask(() => backFirstFocusRef.current?.focus());
    } catch (e: any) {
      setErrorDetail(e?.message ?? "상세 정보를 불러오지 못했습니다.");
    } finally {
      setLoadingDetail(false);
    }
  };

  const closeBack = () => {
    setIsFlipped(false);
    queueMicrotask(() => frontBtnRef.current?.focus());
  };

  // 카드 바깥 클릭으로 닫기 (선택 로직: 플립 상태에서만 동작)
  const wrapperRef = useRef<HTMLDivElement | null>(null);
  useEffect(() => {
    if (!isFlipped) return;
    function onDocClick(e: MouseEvent) {
      if (!wrapperRef.current) return;
      if (!wrapperRef.current.contains(e.target as Node)) {
        closeBack();
      }
    }
    document.addEventListener("mousedown", onDocClick);
    return () => {
      document.removeEventListener("mousedown", onDocClick);
      abortRef.current?.abort();
    };
  }, [isFlipped]);

  // 키보드 접근성: Enter 토글, Esc 닫기
  const onKeyDown: React.KeyboardEventHandler<HTMLDivElement> = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      if (isFlipped) {
        closeBack();
      } else {
        void openBack();
      }
    } else if (e.key === "Escape") {
      if (isFlipped) {
        e.preventDefault();
        closeBack();
      }
    }
  };

  return (
    <div
      ref={wrapperRef}
      className={`relative ${className ?? ""}`}
      role="group"
      aria-label={`${detail.title} 카드`}
      tabIndex={0}
      onKeyDown={onKeyDown}
      style={{ perspective: 1000 }} // Tailwind에 없는 유틸
    >
      <div
        className="relative w-[400px] h-[240px] transition-transform duration-[400ms] ease-out will-change-transform"
        style={{
          transformStyle: "preserve-3d",
          transform: isFlipped ? "rotateY(180deg)" : "rotateY(0deg)",
        }}
      >
        {/* FRONT */}
        <article
          className="absolute inset-0 rounded-2xl bg-white shadow-md hover:shadow-lg border border-gray-100 overflow-hidden"
          style={{ backfaceVisibility: "hidden" }}
          aria-hidden={isFlipped}
        >
          {/* 이미지 영역 (60%) */}
          <div className="w-full h-[144px] bg-gray-100 flex items-center justify-center overflow-hidden">
            {!thumbError && detail.thumbnailUrl ? (
              <img
                src={detail.thumbnailUrl}
                alt={`${detail.title} 썸네일`}
                className="w-full h-full object-cover"
                loading="lazy"
                onError={() => setThumbError(true)}
              />
            ) : (
              <span className="text-gray-500 text-sm">이미지 미등록</span>
            )}
          </div>

          {/* 제목 영역 (20%) */}
          <div className="px-4 h-[48px] flex items-center justify-between">
            <h3 className="font-semibold text-base truncate" title={detail.title}>
              {detail.title}
            </h3>
            {detail.categoryName && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-600">
                {detail.categoryName}
              </span>
            )}
          </div>

          {/* 버튼 영역 (20%) */}
          <div className="h-[48px] px-4 pb-3 flex items-center justify-between gap-2">
            <button
              ref={frontBtnRef}
              onClick={openBack}
              aria-expanded={isFlipped}
              aria-controls={`card-back-${detail.id}`}
              className="flex-1 h-9 rounded-lg border border-gray-200 hover:bg-gray-50 text-sm"
            >
              세부정보 보기
            </button>
            <button
              onClick={() => navigate(startHref)}
              className="flex-1 h-9 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm"
            >
              시작하기
            </button>
          </div>
        </article>

        {/* BACK */}
        <article
          id={`card-back-${detail.id}`}
          className="absolute inset-0 rounded-2xl bg-white shadow-md border border-gray-100 p-4 flex flex-col"
          style={{ transform: "rotateY(180deg)", backfaceVisibility: "hidden" }}
          aria-hidden={!isFlipped}
          aria-live="polite"
        >
          {/* 상단 메타 */}
          <div className="flex items-center justify-between mb-2">
            <h3 className="font-semibold text-base truncate" title={detail.title}>
              {detail.title}
            </h3>
            <div className="flex items-center gap-2 shrink-0">
              {detail.difficulty && (
                <span
                  className={`text-xs px-2 py-0.5 rounded-full ${
                    detail.difficulty === "HARD"
                      ? "bg-red-50 text-red-600"
                      : "bg-emerald-50 text-emerald-600"
                  }`}
                  title={`난이도: ${detail.difficulty}`}
                >
                  {detail.difficulty}
                </span>
              )}
              {typeof detail.totalSequences === "number" && (
                <span className="text-xs text-gray-500">
                  {detail.totalSequences} 단계
                </span>
              )}
            </div>
          </div>

          {/* 본문 (요약 / 로딩 / 에러) */}
          <div className="relative flex-1">
            {loadingDetail && (
              <div className="animate-pulse space-y-2">
                <div className="h-4 bg-gray-100 rounded" />
                <div className="h-4 bg-gray-100 rounded w-11/12" />
                <div className="h-4 bg-gray-100 rounded w-10/12" />
                <div className="h-4 bg-gray-100 rounded w-8/12" />
              </div>
            )}
            {!loadingDetail && errorDetail && (
              <p className="text-sm text-red-600">{errorDetail}</p>
            )}
            {!loadingDetail && !errorDetail && (
              <p className="text-sm text-gray-700 leading-5 line-clamp-6 whitespace-pre-line">
                {detail.summary || "요약 정보가 없습니다."}
              </p>
            )}
          </div>

          {/* 하단 푸터 */}
          <div className="mt-3 flex items-center justify-between gap-2">
            <button
              ref={backFirstFocusRef}
              onClick={() => navigate(startHref)}
              className="flex-1 h-9 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm"
            >
              시작하기
            </button>
            <button
              onClick={closeBack}
              className="flex-1 h-9 rounded-lg border border-gray-200 hover:bg-gray-50 text-sm"
            >
              닫기
            </button>
          </div>

          {(detail.updatedAt || detail.createdAt) && (
            <div className="mt-2 text-[11px] text-gray-500">
              업데이트: {formatDate(detail.updatedAt || detail.createdAt!)}
            </div>
          )}
        </article>
      </div>
    </div>
  );
}

// YYYY.MM.DD 포맷 유틸
function formatDate(iso: string) {
  try {
    const d = new Date(iso);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}.${m}.${day}`;
  } catch {
    return iso;
  }
}
