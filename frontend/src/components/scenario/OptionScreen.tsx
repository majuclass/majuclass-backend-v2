/** @format */

import type { Option } from "../../types/Scenario";

type OptionScreenProps = {
  options: Option[];
  onSelect: (option: Option) => void;
};

export default function OptionScreen({ options, onSelect }: OptionScreenProps) {
  return (
    <div>
      {options.map((option) => (
        <button key={option.optionId} onClick={() => onSelect(option)}>
          {option.optionText}
        </button>
      ))}
    </div>
  );
}
