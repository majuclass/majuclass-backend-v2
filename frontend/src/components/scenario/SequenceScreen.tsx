/** @format */

import type { Sequence } from "../../types/Scenario";
import DialogueBox from "./DialogueBox";

/** @format */

type SequenceScreenProps = {
  sequence: Sequence;
  onNext: () => void;
};

export default function SequenceScreen({
  sequence,
  onNext,
}: SequenceScreenProps) {
  return (
    <div className="relative h-screen w-screen flex flex-col items-center justify-end bg-center bg-cover">
      {/* 대화 상자 */}
      <div className="relative z-20 w-full flex justify-center">
        <DialogueBox
          speaker="직원"
          text={sequence.question}
          showNextButton
          onNext={onNext}
        />
      </div>
    </div>
  );
}
