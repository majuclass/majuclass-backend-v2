import { create } from "zustand";

interface UserState {
  studentId: number | null;
  studentName: string | null;

  pendingScenarioId: number | null;
  isStudentModalOpen: boolean;

  setStudent: (id: number, name: string) => void;

  openStudentModal: (scenarioId: number) => void;
  closeStudentModal: () => void;

  clear: () => void;
}

export const useUserStore = create<UserState>((set) => ({
  studentId: null,
  studentName: null,
  pendingScenarioId: null,
  isStudentModalOpen: false,

  setStudent: (id, name) =>
    set({
      studentId: id,
      studentName: name,
      isStudentModalOpen: false,
    }),

  openStudentModal: (scenarioId) =>
    set({
      isStudentModalOpen: true,
      pendingScenarioId: scenarioId,
    }),

  closeStudentModal: () =>
    set({
      isStudentModalOpen: false,
      pendingScenarioId: null,
    }),

  clear: () => set({ studentId: null, studentName: null }),
}));
