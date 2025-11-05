/** @format */
import { useState } from "react";
import TextInput from "../TextInput";
import type { Option } from "../../types/Scenario";

export default function SequenceInput() {
  const [options, setOptions] = useState<Option[]>([
    {
      optionId: 1,
      optionNumber: 1,
      optionText: "",
      isAnswer: true,
    },
    {
      optionId: 2,
      optionNumber: 2,
      optionText: "",
      isAnswer: false,
    },
  ]);

  //   새 id 추적
  const newOptionId = options.length + 1;

  const handleAddOption = () => {
    const newOption: Option = {
      optionId: newOptionId,
      //   TODO: optionNumber 정체 찾기
      optionNumber: newOptionId,
      optionText: "",
      isAnswer: false,
    };

    setOptions((prevOptions) => [...prevOptions, newOption]);
  };

  return (
    <div>
      <p>질문답변 set</p>
      <div>
        <TextInput name="question" placeholder="질문을 입력하세요">
          질문
        </TextInput>
      </div>
      <div>
        <p>답변(최대 4개)</p>
        <div className="flex flex-wrap gap-4 justify-start">
          {options.map((option, idx) => (
            <div
              key={option.optionId}
              className="w-[48%] min-w-[300px] border p-4 rounded-lg"
            >
              <div className="flex items-center gap-3">
                <input type="radio" name="isAnswer" value={idx} />
                <TextInput
                  name={`answer-${idx}`}
                  placeholder="정답을 입력하세요"
                >
                  정답 여부
                </TextInput>
              </div>
            </div>
          ))}

          {/* 새 옵션 추가 */}
          {options.length < 4 && (
            <div className="w-[48%] flex items-start justify-center p-4">
              <button
                onClick={handleAddOption}
                className="w-full h-full border border-dashed rounded-lg p-4 text-gray-500 hover:bg-gray-50"
              >
                추가하기
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
