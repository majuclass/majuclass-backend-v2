// src/pages/(MainPage)/StudentDashboardPage.tsx
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import NavBar from "../(MainPage)/components/NavBar";

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
  date: string;
  score: number;
  feedback?: string;
};

export default function StudentDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const [student, setStudent] = useState<Student | null>(null);
  const [records, setRecords] = useState<ScenarioRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 실제 API 연결 전 Mock
    const mockStudent: Student = {
      id: Number(id),
      name: "김가람",
      grade: "초3",
      note: "시각자료 선호",
      joinedAt: "2025-10-15",
    };

    const mockRecords: ScenarioRecord[] = [
      {
        id: 1,
        title: "영화 티켓 구매하기",
        date: "2025-10-25",
        score: 92,
        feedback: "대화 응답이 자연스러웠습니다.",
      },
      {
        id: 2,
        title: "카페에서 주문하기",
        date: "2025-10-26",
        score: 87,
        feedback: "발음이 다소 불분명하지만, 주문 순서가 정확했습니다.",
      },
      {
        id: 3,
        title: "횡단보도 건너기",
        date: "2025-10-27",
        score: 95,
        feedback: "상황 인식과 반응 속도가 좋았습니다.",
      },
    ];

    setTimeout(() => {
      setStudent(mockStudent);
      setRecords(mockRecords);
      setLoading(false);
    }, 400);
  }, [id]);

  if (loading || !student) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 text-gray-500">
        로딩 중...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />
      <div className="mx-auto max-w-6xl px-4 py-6">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-semibold">{student.name} 학생 대시보드</h1>
          <button
            onClick={() => history.back()}
            className="rounded-xl border px-3 py-1 text-sm text-gray-600 hover:bg-gray-100"
          >
            ← 목록으로
          </button>
        </div>

        {/* ✅ 기본 정보 카드 */}
        <section className="mb-6 rounded-2xl border bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-lg font-semibold">기본 정보</h2>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <p className="text-gray-500">이름</p>
              <p className="font-medium">{student.name}</p>
            </div>
            <div>
              <p className="text-gray-500">학년/반</p>
              <p className="font-medium">{student.grade ?? "-"}</p>
            </div>
            <div>
              <p className="text-gray-500">비고</p>
              <p className="font-medium">{student.note ?? "-"}</p>
            </div>
            <div>
              <p className="text-gray-500">등록일</p>
              <p className="font-medium">{student.joinedAt}</p>
            </div>
          </div>
        </section>

        {/* ✅ 학습 요약 */}
        <section className="mb-6 rounded-2xl border bg-white p-5 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold">학습 요약</h2>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div className="rounded-xl bg-blue-50 p-4">
              <p className="text-sm text-gray-600">총 참여 시나리오</p>
              <p className="text-2xl font-bold text-blue-600">{records.length}</p>
            </div>
            <div className="rounded-xl bg-green-50 p-4">
              <p className="text-sm text-gray-600">평균 점수</p>
              <p className="text-2xl font-bold text-green-600">
                {records.length
                  ? Math.round(
                      records.reduce((a, b) => a + b.score, 0) / records.length
                    )
                  : "-"}
              </p>
            </div>
            <div className="rounded-xl bg-yellow-50 p-4">
              <p className="text-sm text-gray-600">최근 학습일</p>
              <p className="text-2xl font-bold text-yellow-600">
                {records[0]?.date ?? "-"}
              </p>
            </div>
          </div>
        </section>

        {/* ✅ 시나리오 기록 */}
        <section className="rounded-2xl border bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-lg font-semibold">시나리오 학습 기록</h2>
          <div className="overflow-hidden rounded-xl border">
            <table className="min-w-full divide-y text-sm">
              <thead className="bg-gray-50">
                <tr className="text-left text-gray-600">
                  <th className="px-4 py-2">시나리오명</th>
                  <th className="px-4 py-2">날짜</th>
                  <th className="px-4 py-2">점수</th>
                  <th className="px-4 py-2">피드백</th>
                </tr>
              </thead>
              <tbody className="divide-y bg-white">
                {records.map((r) => (
                  <tr key={r.id} className="hover:bg-gray-50">
                    <td className="px-4 py-2 font-medium">{r.title}</td>
                    <td className="px-4 py-2">{r.date}</td>
                    <td className="px-4 py-2 text-blue-600 font-semibold">
                      {r.score}
                    </td>
                    <td className="px-4 py-2 text-gray-700">
                      {r.feedback ?? "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}
