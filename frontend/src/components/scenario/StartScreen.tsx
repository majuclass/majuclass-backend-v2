/** @format
 * 백그라운드 asset으로 설정
 *
 */

import type { Scenario } from "../../types/Scenario";

type StartScreenProps = {
  scenario: Scenario;
  onStart: () => void;
};
export default function StartScreen({ scenario, onStart }: StartScreenProps) {
  return (
    <div className="flex flex-row-reverse">
      <h2>{scenario.title}</h2>
      <p>{scenario.summary}</p>
      {/* TODO: 난이도 매핑 */}
      <p>난이도: {scenario.difficulty}</p>

      <p>{scenario.categoryName}</p>
      <button onClick={onStart}>실행하기</button>
    </div>
  );
}
