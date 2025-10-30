/** @format
 * 백그라운드 asset으로 설정
 *
 */

import type { Scenario } from "../../../types/Scenario";

type StartScreenProps = {
  scenario: Scenario;
  onStart: () => void;
};
export default function StartScreen({ scenario, onStart }: StartScreenProps) {
  // 난이도 매핑
  const difficultyMap: Record<string, string> = {
    easy: "하",
    medium: "중",
    hard: "상",
  };

  return (
    <div className="h-screen w-screen flex items-center justify-center bg-cover bg-center">
      <div className="backdrop-blur-sm bg-white/80 rounded-2xl shadow-xl p-12 text-center w-full max-w-xl">
        <h2 className="text-2xl font-bold mb-2">{scenario.title}</h2>
        <p className="text-lg text-gray-700 mb-1">{scenario.summary}</p>
        {/* TODO: 난이도 매핑 */}
        <p className="text-xl text-gray-600 mb-6">
          난이도:
          {difficultyMap[scenario.difficulty.toLowerCase()] ||
            scenario.difficulty}
        </p>

        {/* <p>{scenario.categoryName}</p> */}
        <button
          className="bg-orange-400 text-white py-2 px-6 rounded-md font-normal hover:bg-orange-500 transition tracking-wide"
          onClick={onStart}
        >
          실행하기
        </button>
      </div>
    </div>
  );
}
