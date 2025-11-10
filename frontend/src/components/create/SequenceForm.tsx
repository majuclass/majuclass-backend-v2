/** @format */

import { useState } from "react";
import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import TextButton from "../TextButton";
import SequenceInput from "./SequenceInput";
import SequenceTabHeader from "./SequenceTabHeader";

interface SequenceFormProps {
  onPrev: () => void;
}

export default function SequenceForm({ onPrev }: SequenceFormProps) {
  const sequences = useScenarioCreateStore((s) => s.sequences);
  const { addSequence } = useScenarioCreateStore();
  const { deleteSequence } = useScenarioCreateStore();

  //   현재 활성 시퀀스
  const [activeSeqNum, setActiveSeqNum] = useState(1);

  const activeSequence = sequences.find(
    (s) => s.sequenceNumber === activeSeqNum
  );

  // TODO: api.post 로직 추가 및 전송 버튼으로 빼기
  const handleCreateScenario = () => {
    const { title, summary, thumbnail, background, categoryId, sequences } =
      useScenarioCreateStore.getState();

    const scenarioData = {
      title: title,
      summary: summary,
      thumbnail: thumbnail,
      background: background,
      categoryId: categoryId,
      sequences: sequences,
    };

    const json = JSON.stringify(scenarioData);
    console.log(json);
    console.log(scenarioData);

    // TODO: 전송
  };

  return (
    <>
      {/* TODO: 개수 제한  */}
      {/* Header */}
      <div className="flex flex-row flex-wrap items-center gap-2 rounded-2xl p-3">
        {sequences.map((sequence) => (
          <div key={sequence.sequenceNumber}>
            <SequenceTabHeader
              num={sequence.sequenceNumber}
              isActive={activeSeqNum === sequence.sequenceNumber}
              onClick={() => setActiveSeqNum(sequence.sequenceNumber!)}
              onDelete={() => deleteSequence(sequence.sequenceNumber!)}
            />
          </div>
        ))}
        <div>
          <button
            onClick={addSequence}
            className="ml-2 px-3 py-1 rounded-full bg-pink-100 text-pink-600 border border-pink-300 hover:bg-pink-200 transition-all text-sm"
          >
            추가하기
          </button>
        </div>
      </div>

      {/* 시퀀스 */}
      <div>
        {/* 시퀀스 생성 */}
        <div>
          <div className="flex flex-row items-center gap-2 mb-4 bg-pink-50 rounded-2xl justify-center min-h-[600px] transition-all duration-300">
            <SequenceInput activeSeq={activeSequence!}></SequenceInput>
          </div>
        </div>
        {/* 버튼 */}
        <div className="flex flex-row justify-between">
          <TextButton onClick={onPrev}>이전</TextButton>
          <TextButton onClick={handleCreateScenario}>저장</TextButton>
        </div>
      </div>
    </>
  );
}
