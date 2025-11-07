/** @format */

import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import TextButton from "../TextButton";
import SequenceInput from "./SequenceInput";

/** @format */
interface SequenceFormProps {
  onPrev: () => void;
}

export default function SequenceForm({ onPrev }: SequenceFormProps) {
  // 모든 상태 가져오기
  const scenario = useScenarioCreateStore.getState();
  const { title, summary, categoryId, totalSequences, sequences, thumbnail } =
    scenario;

  // TODO: 썸네일 file & blob 알아보기
  const formData = new FormData();
  formData.append(
    "data",
    new Blob(
      [
        JSON.stringify({
          title,
          summary,
          categoryId,
          totalSequences,
          sequences,
        }),
      ],
      { type: "application/json" }
    )
  );
  if (thumbnail) formData.append("thumbnail", thumbnail);

  // TODO: api.post 로직 추가 및 전송 버튼으로 빼기
  const handleSend = () => {};

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
          <TextButton onClick={handleSend}>저장</TextButton>
        </div>
      </div>
    </>
  );
}
