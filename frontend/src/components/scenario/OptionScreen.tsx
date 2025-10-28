/** @format */

import type { Option } from "../../types/Scenario";
import OptionButton from "./OptionButton";

type OptionScreenProps = {
  options: Option[];
  onSelect: (option: Option) => void;
};

export default function OptionScreen({ options, onSelect }: OptionScreenProps) {
  const colors = ["pink", "yellow", "green", "blue"] as const; // 색상 순서 지정

  return (
    <div className="flex flex-wrap justify-center gap-4 p-6 font-shark font-normal">
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
  );
}
