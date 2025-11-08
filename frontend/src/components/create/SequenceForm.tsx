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
      <div className="flex flex-row">
        {sequences.map((sequence) => (
          <div key={sequence.sequenceNumber}>
            <SequenceTabHeader
              num={sequence.sequenceNumber}
              isActive={activeSeqNum === sequence.sequenceNumber}
              onClick={() => setActiveSeqNum(sequence.sequenceNumber!)}
            />
          </div>
        ))}
        <div>
          <button onClick={addSequence}>추가하기</button>
        </div>
      </div>
      <div>
        {/* 시퀀스 생성 */}
        <div>
          <div>
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
