import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../../../stores/useUserStore";
import type { StudentResponse } from "../../../types/MainPage";

type Props = {
  students: StudentResponse[];
};

export default function SelectStudentModal({ students }: Props) {
  const navigate = useNavigate();

  const {
    isStudentModalOpen,
    pendingScenarioId,
    closeStudentModal,
    setStudent,
  } = useUserStore();

  const [selectedId, setSelectedId] = useState<number | null>(null);

  if (!isStudentModalOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-[999]">
      <div className="bg-white rounded-xl w-[350px] p-5">
        <h2 className="text-lg font-bold mb-3">학생을 선택하세요</h2>

        <div className="space-y-2 max-h-[250px] overflow-y-auto">
          {students.map((s) => {
            const isSelected = selectedId === s.studentId;

            return (
              <button
                key={s.studentId}
                onClick={() => setSelectedId(s.studentId)}
                className={`w-full p-3 rounded-lg border text-left flex items-center gap-3
                  ${isSelected ? "bg-blue-100 border-blue-500" : "hover:bg-gray-50"}`}
              >
                <input
                  type="radio"
                  checked={isSelected}
                  onChange={() => setSelectedId(s.studentId)}
                  className="w-4 h-4"
                />

                <span>{s.name}</span>
              </button>
            );
          })}
        </div>

        {/* 버튼 영역 */}
        <div className="mt-4 flex gap-2">
          <button
            onClick={closeStudentModal}
            className="flex-1 py-2 bg-gray-200 rounded-lg"
          >
            취소
          </button>

          <button
            disabled={selectedId === null}
            onClick={() => {
              if (selectedId == null) return;

              const selectedStudent = students.find(
                (s) => s.studentId === selectedId
              );

              if (selectedStudent) {
                setStudent(selectedStudent.studentId, selectedStudent.name);
              }

              closeStudentModal();

              if (pendingScenarioId) {
                navigate(`/simulation/${pendingScenarioId}`);
              }
            }}
            className={`flex-1 py-2 rounded-lg text-white
              ${selectedId ? "bg-blue-600 hover:bg-blue-700" : "bg-blue-300 cursor-not-allowed"}`}
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
