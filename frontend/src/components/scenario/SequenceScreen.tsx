/** @format */

import type { Sequence } from "../../types/Scenario";

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
    <div>
      <h2>시퀀스 진행 스크린</h2>
      <p>시퀀스 번호: {sequence.sequenceNumber}</p>
      <p>질문: {sequence.question}</p>
      <button onClick={onNext}>문제 풀기</button>
    </div>
  );
}
