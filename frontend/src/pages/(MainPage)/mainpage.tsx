// src/pages/(MainPage)/mainpage.tsx
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./components/NavBar";

type Student = { id: number; name: string; className?: string };
type Activity = {
  id: number;
  studentName: string;
  scenarioTitle: string;
  finishedAt: string; // 완료 시간
  correctRate: number; // %
};

export default function MainPage() {
  const navigate = useNavigate();

  // ── Mock (임시 데이터)
  const students: Student[] = useMemo(
    () => [
      { id: 1, name: "김민수", className: "3반" },
      { id: 2, name: "이서윤", className: "2반" },
      { id: 3, name: "박현우", className: "1반" },
    ],
    []
  );

  const recent: Activity[] = useMemo(
    () => [
      { id: 201, studentName: "김민수", scenarioTitle: "카페에서 주문하기", finishedAt: "2025-10-31 14:20", correctRate: 80 },
      { id: 202, studentName: "이서윤", scenarioTitle: "영화관 예매/입장하기", finishedAt: "2025-10-30 09:10", correctRate: 60 },
      { id: 203, studentName: "박현우", scenarioTitle: "버스 타고 이동하기", finishedAt: "2025-10-28 16:05", correctRate: 90 },
    ],
    []
  );

  // ── State
  const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null);
  const [isSelectModalOpen, setIsSelectModalOpen] = useState(false);

  const selectedStudent = useMemo(
    () => students.find((s) => s.id === selectedStudentId) ?? null,
    [selectedStudentId, students]
  );

  // ── Handlers
  const handleStart = () => {
    if (!selectedStudent) {
      setIsSelectModalOpen(true);
      return;
    }
    // 실제로는 시나리오 탐색 페이지로 이동 (학생은 전역/쿼리 등으로 전달)
    // navigate(`/scenario?studentId=${selectedStudent.id}`);
    navigate("/scenario"); // 우선 라우팅만
  };

  const handlePickStudent = (id: number) => {
    setSelectedStudentId(id);
    setIsSelectModalOpen(false);
    // localStorage에 유지하고 싶으면 아래 주석 해제
    // localStorage.setItem("mc:selectedStudentId", String(id));
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      {/* 상단: 학생 선택 + 시작 CTA (단일 기능) */}
      <section className="sticky top-0 z-10 border-b bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-col gap-3 px-4 py-4 md:flex-row md:items-center md:justify-between">
          <div className="flex flex-wrap items-center gap-2">
            <label htmlFor="student" className="text-sm text-gray-600">
              학생 선택
            </label>
            <select
              id="student"
              className="w-64 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={selectedStudentId ?? ""}
              onChange={(e) => setSelectedStudentId(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">학생을 선택하세요</option>
              {students.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} {s.className ? `(${s.className})` : ""}
                </option>
              ))}
            </select>

            {selectedStudent ? (
              <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                선택됨: {selectedStudent.name}
              </span>
            ) : (
              <span className="rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-600">
                아직 선택된 학생이 없습니다
              </span>
            )}
          </div>

          <div className="flex gap-2">
            <button
              className="rounded-xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700 active:scale-[0.99]"
              onClick={handleStart}
            >
              시나리오 시작하기
            </button>
            <button
              className="rounded-xl bg-white px-4 py-2 text-sm font-semibold text-gray-800 ring-1 ring-gray-300 hover:bg-gray-50"
              onClick={() => navigate("/scenarios")}
            >
              시나리오 라이브러리로 이동
            </button>
          </div>
        </div>
      </section>

      {/* 본문: 좌(최근 활동) / 우(KPI 요약)만 남김 */}
      <main className="mx-auto grid max-w-6xl grid-cols-1 gap-6 px-4 py-8 lg:grid-cols-3">
        {/* 최근 활동 (완료 기록만) */}
        <section className="lg:col-span-2 rounded-2xl border border-gray-200 bg-white p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">최근 활동 (완료)</h2>
          </div>

          {recent.length === 0 ? (
            <EmptyLine text="아직 완료 기록이 없습니다. 상단에서 시나리오를 시작해보세요." />
          ) : (
            <ul className="divide-y divide-gray-100">
              {recent.map((r) => (
                <li key={r.id} className="flex items-center justify-between py-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-gray-900">
                      ({r.studentName}) {r.scenarioTitle}
                    </p>
                    <p className="truncate text-xs text-gray-500">
                      완료 {r.finishedAt} · 정답률 {r.correctRate}%
                    </p>
                  </div>
                  <button
                    className="ml-3 shrink-0 rounded-lg bg-white px-3 py-1.5 text-xs font-semibold text-gray-800 ring-1 ring-gray-300 hover:bg-gray-50"
                    onClick={() => alert("상세 리포트(추후 학생 대시보드로 이동)")}
                  >
                    리포트
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        {/* KPI 요약 */}
        <section className="rounded-2xl border border-gray-200 bg-white p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">학습 현황 요약</h2>
            <button
              className="text-sm text-indigo-600 hover:underline"
              onClick={() => alert("학생 관리 > 대시보드로 이동")}
            >
              자세히 보기
            </button>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <KpiCard label="금주 실행" value="12회" />
            <KpiCard label="평균 정답률" value="74%" />
            <KpiCard label="완료 시나리오" value="9개" />
          </div>

          {/* 가벼운 placeholder 시각화 */}
          <div className="mt-4">
            <p className="mb-2 text-xs text-gray-500">상위 3명 최근 정답률</p>
            <div className="grid grid-cols-3 gap-2">
              {["김민수", "이서윤", "박현우"].map((name) => (
                <div key={name} className="rounded-lg border border-gray-200 p-2">
                  <div className="mb-1 text-xs font-medium text-gray-700">{name}</div>
                  <div className="h-8 w-full rounded bg-gray-100" aria-hidden />
                </div>
              ))}
            </div>
          </div>
        </section>
      </main>

      {/* 학생 선택 모달 */}
      {isSelectModalOpen && (
        <div className="fixed inset-0 z-20 flex items-center justify-center bg-black/40 px-4" role="dialog" aria-modal="true">
          <div className="w-full max-w-md rounded-2xl bg-white p-5 shadow-xl">
            <div className="mb-4">
              <h3 className="text-base font-semibold text-gray-900">학생 선택</h3>
              <p className="mt-1 text-sm text-gray-500">시작하기 전에 학생을 선택해주세요.</p>
            </div>

            <div className="max-h-64 space-y-2 overflow-y-auto rounded-lg border border-gray-200 p-2">
              {students.map((s) => (
                <button
                  key={s.id}
                  className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left hover:bg-gray-50"
                  onClick={() => handlePickStudent(s.id)}
                >
                  <span className="text-sm text-gray-800">
                    {s.name} {s.className ? `(${s.className})` : ""}
                  </span>
                  <span className="text-xs text-gray-500">선택</span>
                </button>
              ))}
              <button
                className="mt-1 w-full rounded-lg bg-white px-3 py-2 text-left text-sm text-indigo-700 ring-1 ring-indigo-200 hover:bg-indigo-50"
                onClick={() => alert("학생 추가로 이동")}
              >
                + 새 학생 추가
              </button>
            </div>

            <div className="mt-4 flex justify-end">
              <button
                className="rounded-lg bg-white px-4 py-2 text-sm font-semibold text-gray-800 ring-1 ring-gray-300 hover:bg-gray-50"
                onClick={() => setIsSelectModalOpen(false)}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ───────────── Sub Components ───────────── */

function KpiCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      <div className="text-xs text-gray-500">{label}</div>
      <div className="mt-1 text-2xl font-semibold text-gray-900">{value}</div>
    </div>
  );
}

function EmptyLine({ text }: { text: string }) {
  return <div className="rounded-lg bg-gray-50 p-4 text-center text-sm text-gray-500">{text}</div>;
}
