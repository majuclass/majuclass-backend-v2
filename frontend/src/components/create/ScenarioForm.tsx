/** @format */

import type { CreateScenario } from "../../types/Scenario";
import TextButton from "../TextButton";
import TextInput from "../TextInput";

/** @format */
interface ScenarioFormProps {
  scenarioData: CreateScenario;
  onNext: () => void;
}

export default function ScenarioForm({
  scenarioData,
  onNext,
}: ScenarioFormProps) {
  return (
    <>
      <h1>시나리오 생성</h1>
      <div className="flex flex-row ">
        {/* 메타데이터 입력 */}
        <div>
          <div>
            <TextInput name="title" placeholder="시나리오 제목을 입력하세요">
              시나리오 제목
            </TextInput>
            <TextInput
              name="description"
              placeholder="시나리오 설명을 입력하세요."
            >
              시나리오 설명
            </TextInput>
          </div>
          <div>
            {/* TODO: 카테고리 불러오기*/}
            <p>카테고리</p>
            <select name="category" id="">
              <option value="val1">카테1</option>
              <option value="val2">카테2</option>
              <option value="val3">카테3</option>
            </select>
          </div>
          {/* TODO: 이미지 preview 만들기 */}
          <div>
            <p>썸네일 이미지</p>
            <input type="file" name="thumbnailImg" />
          </div>
          <div>
            <p>배경화면 이미지</p>
            <input type="file" name="backgroundImg" />
          </div>
        </div>
        {/* 썸네일 미리보기 */}
        <div>썸네일 미리보기</div>
      </div>
      <div className="flex flex-row justify-between">
        <TextButton>취소</TextButton>
        <TextButton onClick={onNext}>다음</TextButton>
      </div>
    </>
  );
}
