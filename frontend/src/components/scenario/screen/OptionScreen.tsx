/** @format */

import type { Option, Sequence } from "../../../types/Scenario";
import OptionButton from "../OptionButton";
import girlHead from "../../../assets/scenarios/cinema/cinema-girl-head.png";

type OptionScreenProps = {
  options: Option[];
  sequence: Sequence;
  onSelect: (option: Option) => void;
};

export default function OptionScreen({
  options,
  sequence,
  onSelect,
}: OptionScreenProps) {
  const colors = ["pink", "yellow", "green", "blue"] as const; // 색상 순서 지정

  return (
    <div className="flex flex-col items-center justify-center h-screen p-6">
      <div className="flex items-center">
        {/* 머리머리 */}
        <img src={girlHead} alt="직원 머리" className="w-24 h-auto mb-2" />
        <div className="bg-white rounded-2xl px-6 py-3 text-2xl font-semibold shadow">
          {sequence.question}
        </div>
      </div>

      {/* 선택지 */}
      <div className="flex flex-wrap justify-center gap-4 p-6 font-shark font-normal text-2xl">
        {options.map((option, index) => (
          <OptionButton
            key={option.optionId}
            color={colors[index % colors.length]}
            onClick={() => onSelect(option)}
          >
            {option.optionText}
          </OptionButton>
        ))}
      </div>
    </div>
  );
}
