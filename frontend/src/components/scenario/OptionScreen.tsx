/** @format */

import type { Option } from "../../types/Scenario";
import OptionButton from "./OptionButton";

type OptionScreenProps = {
  options: Option[];
  onSelect: (option: Option) => void;
};

export default function OptionScreen({ options, onSelect }: OptionScreenProps) {
  return (
    <div className="flex flex-wrap justify-center gap-4 p-6">
      {options.map((option) => (
        <OptionButton key={option.optionId} onClick={() => onSelect(option)}>
          {option.optionText}
        </OptionButton>
      ))}
    </div>
  );
}
