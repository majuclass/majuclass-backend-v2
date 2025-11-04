// src/pages/(MainPage)/StudentManagementPage.tsx
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "../(MainPage)/components/NavBar"

type Student = {
  id: number;
  name: string;
  grade?: string;
  note?: string;
  createdAt: string;
};

export default function StudentManagementPage() {
  const navigate = useNavigate();

  // ---- Mock 데이터(초안) ----
  const [students, setStudents] = useState<Student[]>([
    { id: 1, name: "김가람", grade: "초3", createdAt: new Date().toISOString() },
    { id: 2, name: "박도윤", grade: "중1", createdAt: new Date().toISOString() },
    { id: 3, name: "이서준", grade: "고1", createdAt: new Date().toISOString() },
  ]);

  // ---- 수동 입력 상태 ----
  const [name, setName] = useState("");
  const [grade, setGrade] = useState("");
  const [note, setNote] = useState("");

  // ---- 파일 업로드 상태(표시용) ----
  const [files, setFiles] = useState<File[]>([]);
  const fileAccept =
    ".xlsx,.csv,.doc,.docx,.docs,.hwp,.pdf"; // 요청한 확장자 기준(.docs 포함)

  const handleAddManual = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    const next: Student = {
      id: Date.now(),
      name: name.trim(),
      grade: grade.trim() || undefined,
      note: note.trim() || undefined,
      createdAt: new Date().toISOString(),
    };
    setStudents((prev) => [next, ...prev]);
    setName("");
    setGrade("");
    setNote("");
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;
    setFiles(Array.from(e.target.files));
  };

  const handleFileUpload = async () => {
    if (files.length === 0) return alert("업로드할 파일을 선택하세요.");
    // 초안: 서버 API 준비 전이므로 실제 업로드/파싱은 스킵
    // 예시: FormData로 전송
    // const fd = new FormData();
    // files.forEach((f) => fd.append("files", f));
    // await fetch("/api/students/bulk-upload", { method: "POST", body: fd });
    alert(`${files.length}개 파일 업로드(목업)`);
    setFiles([]);
  };

  // ---- 검색(간단) ----
  const [q, setQ] = useState("");
  const filtered = useMemo(
    () =>
      students.filter(
        (s) =>
          s.name.toLowerCase().includes(q.toLowerCase()) ||
          (s.grade ?? "").includes(q)
      ),
    [q, students]
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-7xl px-4 py-6">
        <h1 className="mb-4 text-2xl font-semibold">학생 관리</h1>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* 좌측 패널: 수동 입력 & 파일 업로드 */}
          <div className="space-y-6 lg:col-span-1">
            {/* 수동 입력 카드 */}
            <section className="rounded-2xl border bg-white p-4 shadow-sm">
              <h2 className="mb-3 text-lg font-semibold">수동으로 학생 추가</h2>
              <form onSubmit={handleAddManual} className="space-y-3">
                <div>
                  <label className="mb-1 block text-sm text-gray-600">이름</label>
                  <input
                    className="w-full rounded-xl border px-3 py-2 outline-none focus:ring"
                    placeholder="예) 김가람"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="mb-1 block text-sm text-gray-600">
                      학년/반(선택)
                    </label>
                    <input
                      className="w-full rounded-xl border px-3 py-2 outline-none focus:ring"
                      placeholder="예) 초3 / 3-2"
                      value={grade}
                      onChange={(e) => setGrade(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="mb-1 block text-sm text-gray-600">
                      비고(선택)
                    </label>
                    <input
                      className="w-full rounded-xl border px-3 py-2 outline-none focus:ring"
                      placeholder="예) 시각자료 선호"
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full rounded-xl bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                >
                  추가
                </button>
              </form>
            </section>

            {/* 파일 업로드 카드 */}
            <section className="rounded-2xl border bg-white p-4 shadow-sm">
              <h2 className="mb-3 text-lg font-semibold">
                파일로 추가 
              </h2>

              <div className="rounded-xl border border-dashed p-4 text-center">
                <input
                  type="file"
                  multiple
                  accept={fileAccept}
                  onChange={handleFileChange}
                  className="block w-full cursor-pointer rounded-lg border p-2 file:mr-3 file:rounded-md file:border-0 file:bg-gray-100 file:px-3 file:py-2"
                />
                <p className="mt-2 text-xs text-gray-500">
                  템플릿 예시: 이름, 학년/반, 비고 컬럼
                </p>
              </div>

              {files.length > 0 && (
                <ul className="mt-3 max-h-28 overflow-auto rounded-lg border bg-gray-50 p-2 text-sm">
                  {files.map((f) => (
                    <li key={f.name} className="truncate">
                      • {f.name}
                    </li>
                  ))}
                </ul>
              )}

              <button
                onClick={handleFileUpload}
                className="mt-3 w-full rounded-xl bg-gray-800 px-4 py-2 text-white hover:bg-black"
              >
                업로드(목업)
              </button>
            </section>
          </div>

          {/* 우측 패널: 학생 목록 */}
          <div className="lg:col-span-2">
            <section className="rounded-2xl border bg-white p-4 shadow-sm">
              <div className="mb-3 flex items-center justify-between gap-3">
                <h2 className="text-lg font-semibold">학생 목록</h2>
                <input
                  value={q}
                  onChange={(e) => setQ(e.target.value)}
                  placeholder="이름/학년 검색"
                  className="w-56 rounded-xl border px-3 py-2 text-sm outline-none focus:ring"
                />
              </div>

              <div className="overflow-hidden rounded-xl border">
                <table className="min-w-full divide-y">
                  <thead className="bg-gray-50">
                    <tr className="text-left text-sm text-gray-600">
                      <th className="px-4 py-2">이름</th>
                      <th className="px-4 py-2">학년/반</th>
                      <th className="px-4 py-2">등록일</th>
                      <th className="px-4 py-2 text-right">대시보드</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y bg-white text-sm">
                    {filtered.map((s) => (
                      <tr
                        key={s.id}
                        className="hover:bg-gray-50"
                        onClick={() => navigate(`/students/${s.id}`)}
                      >
                        <td className="cursor-pointer px-4 py-2 font-medium">
                          {s.name}
                        </td>
                        <td className="px-4 py-2">{s.grade ?? "-"}</td>
                        <td className="px-4 py-2">
                          {new Date(s.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-4 py-2 text-right">
                          <button
                            onClick={(e) => {
                              e.stopPropagation(); // 행 클릭과 분리
                              navigate(`/students/${s.id}`);
                            }}
                            className="rounded-lg border px-3 py-1 hover:bg-gray-100"
                          >
                            보기
                          </button>
                        </td>
                      </tr>
                    ))}

                    {filtered.length === 0 && (
                      <tr>
                        <td
                          colSpan={4}
                          className="px-4 py-10 text-center text-sm text-gray-400"
                        >
                          학생이 없습니다.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
}
