/** @format */

import { create } from "zustand";
import type { Option } from "../types/Scenario";

// interface CreateScenarioStore extends CreateScenario {
//     // actions 정의
// }

// options부터 가자
interface OptionStore {
  //   states
  options: Option[];
  //   actions
  addOption: (newOption: Option) => void;
}

export const useOptionStore = create<OptionStore>((set) => ({
  options: [],
  addOption: (newOption) =>
    set((state) => ({
      options: [...state.options, newOption],
    })),
}));

/** 시나리오 종합하고 모아서 보낼 total Scenario
 * Scenario Info update? create?
 * Seqeuence update? create ? (at least 1)
 * option update? create? (at least 2)
 *
 * 불변성 유지하며 배열 요소 수정 -> action 내에서
 * 컴포넌트는 action만 호출
 */
// const useScenarioCreateStore = create<CreateScenarioStore>();
