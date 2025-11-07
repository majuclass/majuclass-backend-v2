/** @format */

import { create } from "zustand";

// SSOT 생성
/** 시나리오 종합하고 모아서 보낼 total Scenario
 * Scenario Info update? create?
 * Seqeuence update? create ? (at least 1)
 * option update? create? (at least 2)
 *
 * 불변성 유지하며 배열 요소 수정 -> action 내에서
 * 컴포넌트는 action만 호출
 */

interface OptionData {
  optionNumber?: number;
  optionText: string;
  answer: boolean;
}
interface SequenceData {
  sequenceNumber?: number;
  question: string;
  hasNext: boolean;
  options: OptionData[];
}

// 최종적인 Scenario States
interface ScenarioData {
  title: string;
  summary: string;
  thumbnail: File | null;
  background: File | null;
  categoryId: number;
  totalSequences: number;
  sequences: SequenceData[];
}

// Props로 넘겨줄 Store interface (actions + states)
export interface ScenarioCreateStore extends ScenarioData {
  // actions만 정의
  setScenarioInfo: (info: Partial<ScenarioData>) => void;
  // 자동 증가
  addSequence: () => void;
  updateSequence: (seqNum: number, updatedData: Partial<SequenceData>) => void;
  // 외부에서 받아 증가
  addOption: (seqNum: number, newOption: OptionData) => void;
  resetScenario: () => void;
}

export const useScenarioCreateStore = create<ScenarioCreateStore>((set) => ({
  // state default settings
  title: "",
  summary: "",
  thumbnail: null,
  background: null,
  categoryId: 0,
  totalSequences: 0,
  sequences: [],

  // actions defined in the props
  setScenarioInfo: (info) => {
    set((state) => ({ ...state, ...info }));
  },
  addSequence: () => {
    set((state) => ({
      sequences: [
        ...state.sequences,
        {
          sequenceNumber: state.sequences.length + 1,
          question: "",
          hasNext: false,
          options: [],
        },
      ],
      totalSequences: state.totalSequences + 1,
    }));
  },
  updateSequence: (seqNum, updatedData) => {
    set((state) => ({
      sequences: state.sequences.map((s) =>
        s.sequenceNumber === seqNum ? { ...s, ...updatedData } : s
      ),
    }));
  },
  addOption: (seqNum, newOption) => {
    set((state) => ({
      sequences: state.sequences.map((s) =>
        s.sequenceNumber === seqNum
          ? {
              ...s,
              options: [
                ...s.options,
                { optionNumber: s.options.length + 1, ...newOption },
              ],
            }
          : s
      ),
    }));
  },

  resetScenario: () => {
    set({
      title: "",
      summary: "",
      thumbnail: null,
      background: null,
      categoryId: 0,
      totalSequences: 0,
      sequences: [],
    });
  },
}));
