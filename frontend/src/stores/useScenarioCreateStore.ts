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

export interface OptionData {
  optionNo?: number;
  optionText: string;
  optionPic?: File | null; // 우선 유지
  optionS3Key: string; // S3 업로드 후 받은 키 저장 추가
  optionIconUrl?: string // 추가
  isAnswer: boolean;
}

export interface SequenceData {
  seqNo?: number;
  question: string;
  hasNext?: boolean; // TODO: REMOVE
  options: OptionData[];
}

// 최종적인 Scenario States
export interface ScenarioData {
  title: string;
  summary: string;
  thumbnail: File | null;
  background: File | null;
  categoryId: number;
  totalSequences: number; // totalSequences 꼭 필요할지?
  sequences: SequenceData[];
}

// Props로 넘겨줄 Store interface (actions + states)
export interface ScenarioCreateStore extends ScenarioData {
  // actions만 정의
  setScenarioInfo: (info: Partial<ScenarioData>) => void;
  // 자동 증가
  addSequence: () => void;
  updateSequence: (seqNum: number, updatedData: Partial<SequenceData>) => void;
  deleteSequence: (seqNum: number) => void;
  // 외부에서 받아 증가
  addOption: (seqNum: number, newOption: OptionData) => void;
  updateOption: () => void;
  deleteOption: (seqNum: number, optNum: number) => void;
  resetScenario: () => void;
}

export const useScenarioCreateStore = create<ScenarioCreateStore>((set) => ({
  // state default settings
  title: "",
  summary: "",
  thumbnail: null,
  background: null,
  categoryId: 1,
  totalSequences: 1,
  sequences: [
    {
      seqNo: 1,
      question: "",
      options: [
        { optionNo: 1, optionText: "", optionPic: null, optionS3Key: "", isAnswer: true },
        { optionNo: 2, optionText: "", optionPic: null, optionS3Key: "", isAnswer: false },
      ],
    },
  ], // 시퀀스 최소 1개 두고 시작

  // actions defined in the props
  setScenarioInfo: (info) => {
    set((state) => ({ ...state, ...info }));
  },
  addSequence: () => {
    set((state) => {
      const newSeqNum = state.sequences.length + 1;
      const defaultOptions = [
        { optionNo: 1, optionText: "", optionPic: null, optionS3Key: "", isAnswer: true },
        { optionNo: 2, optionText: "", optionPic: null, optionS3Key: "", isAnswer: false },
      ];

      return {
        ...state,
        sequences: [
          ...state.sequences,
          {
            seqNo: newSeqNum,
            question: "",
            options: defaultOptions,
          },
        ],
        totalSequences: state.totalSequences + 1,
      };
    });
  },
  updateSequence: (seqNum, updatedData) => {
    set((state) => ({
      sequences: state.sequences.map((s) =>
        s.seqNo === seqNum ? { ...s, ...updatedData } : s
      ),
    }));
  },

  deleteSequence: (seqNum) => {
    set((state) => {
      const filtered = state.sequences.filter((s) => s.seqNo !== seqNum);
      // 번호 재정렬 (sequenceNumber 1부터 다시)
      const resequenced = filtered.map((s, idx) => ({
        ...s,
        seqNo: idx + 1,
      }));

      return {
        ...state,
        sequences: resequenced,
        totalSequences: resequenced.length,
      };
    });
  },

  addOption: (seqNum, newOption) => {
    set((state) => ({
      sequences: state.sequences.map((s) =>
        s.seqNo === seqNum
          ? {
              ...s,
              options: [
                ...s.options,
                { optionNo: s.options.length + 1, ...newOption },
              ],
            }
          : s
      ),
    }));
  },

  updateOption: () => {},

  deleteOption: (seqNum, optionNum) => {
    set((state) => ({
      sequences: state.sequences.map((s) =>
        s.seqNo === seqNum
          ? {
              ...s,
              options: s.options
                .filter((o) => o.optionNo !== optionNum)
                .map((o, idx) => ({
                  ...o,
                  optionNo: idx + 1, // 번호 다시 붙이기
                })),
            }
          : s
      ),
    }));
  },

  resetScenario: () => {
    set({
      title: "",
      summary: "",
      categoryId: 1,
      thumbnail: null,
      background: null,
      totalSequences: 1,
      sequences: [],
    });
  },
}));
