/** @format */

import TextButton from "../TextButton";
import SequenceInput from "./SequenceInput";

/** @format */
interface SequenceFormProps {
  onPrev: () => void;
}

export default function SequenceForm({ onPrev }: SequenceFormProps) {
  return (
    <>
      <h1>시퀀스 생성</h1>
      <div>
        {/* 시퀀스 생성 */}
        <div>
          <div>
            질문답변스
            <SequenceInput></SequenceInput>
          </div>
        </div>
        {/* 버튼 */}
        <div className="flex flex-row">
          <TextButton onClick={onPrev}>이전</TextButton>
          <TextButton>저장</TextButton>
        </div>
      </div>
    </>
  );
}
