/** @format */
import { useEffect, useRef } from "react";
import type { Sequence } from "../../../types/Scenario";
import DialogueBox from "../DialogueBox";
import { useTTS } from "../audio/UseTTS";

type SequenceScreenProps = {
  sequence: Sequence;
  onNext: () => void;
};

export default function SequenceScreen({ sequence, onNext }: SequenceScreenProps) {
  const { play } = useTTS();
  const firstMountRef = useRef(true);

  useEffect(() => {
    if (firstMountRef.current) {
      firstMountRef.current = false;
      play(sequence.question);
    }
  }, []);

  return (
    <div className="relative h-screen w-screen flex flex-col items-center justify-end bg-center bg-cover">
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
