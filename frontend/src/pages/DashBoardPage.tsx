// src/pages/(MainPage)/StudentDashboardPage.tsx
import { useEffect, useMemo, useState } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import NavBar from "../components/NavBar";

type Student = {
  id: number;
  name: string;
  grade?: string;
  note?: string;
  joinedAt: string;
};

type ScenarioRecord = {
  id: number;
  title: string;
  date: string;   // YYYY-MM-DD
  score: number;  // 0~100
  feedback?: string;
};

export default function StudentDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const [sp] = useSearchParams(); // ?view=parent|teacher
  const [student, setStudent] = useState<Student | null>(null);
  const [records, setRecords] = useState<ScenarioRecord[]>([]);
  const [loading, setLoading] = useState(true);

  const viewMode = (sp.get("view") ?? "parent") as "parent" | "teacher";

  useEffect(() => {
    // ✅ 실제 API 연결 전 Mock
    const mockStudent: Student = {
      id: Number(id),
      name: "김가람",
      grade: "초3",
      note: "시각자료 선호",
      joinedAt: "2025-10-15",
    };
    const mockRecords: ScenarioRecord[] = [
      { id: 3, title: "횡단보도 건너기", date: "2025-10-27", score: 95, feedback: "상황 인식과 반응 속도가 좋았습니다." },
      { id: 2, title: "카페에서 주문하기", date: "2025-10-26", score: 87, feedback: "발음이 다소 불분명하지만, 주문 순서가 정확했습니다." },
      { id: 1, title: "영화 티켓 구매하기", date: "2025-10-25", score: 92, feedback: "대화 응답이 자연스러웠습니다." },
    ];
    setTimeout(() => {
      setStudent(mockStudent);
      setRecords(mockRecords);
      setLoading(false);
    }, 300);
  }, [id]);

  const stats = useMemo(() => {
    if (!records.length) return { avg: "-", last: "-", count: 0 };
    const count = records.length;
    const avg = Math.round(records.reduce((a, b) => a + b.score, 0) / count);
    const last = records[0]?.date ?? "-"; // 최신이 위에 오도록 정렬했다고 가정
    return { avg, last, count };
  }, [records]);

  const badges = useMemo(() => {
    // ✅ 간단 키워드로 성장/보완 포인트 추출
    const strengths = new Set<string>();
    const practices = new Set<string>();
    for (const r of records) {
      const fb = (r.feedback ?? "").toLowerCase();
      if (fb.includes("자연") || fb.includes("정확")) strengths.add("정확한 순서");
      if (fb.includes("반응") || fb.includes("상황")) strengths.add("상황 인식");
      if (fb.includes("발음") || fb.includes("불분명")) practices.add("발음 명료도");
      if (r.score >= 90) strengths.add("높은 집중도");
      if (r.score < 90) practices.add("피드백 재연습");
    }
    return {
      strengths: Array.from(strengths).slice(0, 4),
      practices: Array.from(practices).slice(0, 4),
    };
  }, [records]);

  const sparkline = useMemo(() => {
    // ✅ 최근 점수 스파크라인 (SVG)
    const data = [...records].reverse().map(r => r.score); // 오래된→최신
    const width = 220, height = 48, pad = 6;
    if (!data.length) return { d: "", width, height };
    const xs = data.map((_, i) => pad + (i * (width - pad * 2)) / Math.max(1, data.length - 1));
    const min = Math.min(...data), max = Math.max(...data);
    const ys = data.map(v => {
      const t = (v - min) / (Math.max(1, max - min));
      return height - pad - t * (height - pad * 2);
    });
    const d = xs.map((x, i) => `${i ? "L" : "M"} ${x.toFixed(1)} ${ys[i].toFixed(1)}`).join(" ");
    return { d, width, height };
  }, [records]);

  if (loading || !student) {
    return <div className="flex min-h-screen items-center justify-center bg-gray-50 text-gray-500">로딩 중...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />
      <div className="mx-auto max-w-5xl px-4 py-6">
        {/* 헤더 */}
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">
              {viewMode === "parent" ? "학부모용 활동 보고서" : "학생 대시보드"} — {student.name}
            </h1>
            <p className="mt-1 text-sm text-gray-600">
              {student.grade ?? "-"} | 등록일 {student.joinedAt} {student.note ? `| ${student.note}` : ""}
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => window.print()}
              className="rounded-xl border px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100"
            >
              PDF로 내보내기/인쇄
            </button>
            <button
              onClick={() => history.back()}
              className="rounded-xl border px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
            >
              ← 목록으로
            </button>
          </div>
        </div>

        {/* 학습 요약 */}
        <section className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          <div className="rounded-2xl border bg-white p-4 shadow-sm">
            <p className="text-sm text-gray-600">총 활동 수</p>
            <p className="mt-1 text-2xl font-extrabold text-blue-600">{stats.count}</p>
          </div>
          <div className="rounded-2xl border bg-white p-4 shadow-sm">
            <p className="text-sm text-gray-600">평균 점수</p>
            <p className="mt-1 text-2xl font-extrabold text-green-600">
              {String(stats.avg)}
            </p>
            {/* 스파크라인 */}
            <svg className="mt-2 w-full" width={sparkline.width} height={sparkline.height} viewBox={`0 0 ${sparkline.width} ${sparkline.height}`}>
              <path d={sparkline.d} fill="none" stroke="currentColor" strokeWidth="2" />
            </svg>
            <p className="mt-1 text-xs text-gray-500">최근 점수 추세</p>
          </div>
          <div className="rounded-2xl border bg-white p-4 shadow-sm">
            <p className="text-sm text-gray-600">최근 활동일</p>
            <p className="mt-1 text-2xl font-extrabold text-yellow-600">{stats.last}</p>
          </div>
        </section>

        {/* 성장 포인트 / 연습 포인트 */}
        <section className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="rounded-2xl border bg-white p-5 shadow-sm">
            <h2 className="mb-2 text-lg font-semibold">성장 포인트</h2>
            {badges.strengths.length ? (
              <div className="flex flex-wrap gap-2">
                {badges.strengths.map((b) => (
                  <span key={b} className="rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-700">
                    {b}
                  </span>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500">요약할 내용이 아직 없습니다.</p>
            )}
          </div>
          <div className="rounded-2xl border bg-white p-5 shadow-sm">
            <h2 className="mb-2 text-lg font-semibold">다음 연습 목표</h2>
            {badges.practices.length ? (
              <div className="flex flex-wrap gap-2">
                {badges.practices.map((b) => (
                  <span key={b} className="rounded-full bg-yellow-50 px-3 py-1 text-xs font-semibold text-yellow-700">
                    {b}
                  </span>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500">보완이 필요한 항목이 발견되면 표시됩니다.</p>
            )}
          </div>
        </section>

        {/* 활동 타임라인(학부모 친화 표기) */}
        <section className="rounded-2xl border bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-lg font-semibold">최근 활동</h2>
          <div className="overflow-hidden rounded-xl border">
            <table className="min-w-full divide-y text-sm">
              <thead className="bg-gray-50">
                <tr className="text-left text-gray-600">
                  <th className="px-4 py-2">날짜</th>
                  <th className="px-4 py-2">활동 내용</th>
                  <th className="px-4 py-2">결과</th>
                  <th className="px-4 py-2">코멘트</th>
                </tr>
              </thead>
              <tbody className="divide-y bg-white">
                {records.map((r) => (
                  <tr key={r.id} className="hover:bg-gray-50">
                    <td className="px-4 py-2">{r.date}</td>
                    <td className="px-4 py-2">{r.title}</td>
                    <td className="px-4 py-2 font-semibold text-blue-600">{r.score}점</td>
                    <td className="px-4 py-2 text-gray-700">{r.feedback ?? "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {/* 인쇄 최적화: 버튼 등 숨김 */}
      <style>{`@media print{
        nav, .print\\:hidden { display: none !important; }
        body { background:#fff; }
      }`}</style>
    </div>
  );
}
