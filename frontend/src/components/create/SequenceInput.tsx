/** @format */
import TextInput from "../TextInput";
import {
  useScenarioCreateStore,
  type SequenceData,
} from "../../stores/useScenarioCreateStore";

interface SequenceInputProps {
  activeSeq: SequenceData;
}

export default function SequenceInput({ activeSeq }: SequenceInputProps) {
  // 현재 sequence
  const activeSequenceData = useScenarioCreateStore((s) =>
    s.sequences.find((seq) => seq.sequenceNumber === activeSeq.sequenceNumber)
  );

  const addOption = useScenarioCreateStore((s) => s.addOption);
  const updateSequence = useScenarioCreateStore((s) => s.updateSequence);

  if (!activeSequenceData) return null;

  const options = activeSequenceData.options;

  // 이벤트 핸들러

  const handleOptionChange = (idx: number, text: string) => {
    const newOptions = [...options];
    newOptions[idx] = { ...newOptions[idx], optionText: text };

    updateSequence(activeSequenceData.sequenceNumber!, {
      options: newOptions,
    });
  };

  const handleAddOption = () => {
    if (options.length >= 4) return;

    const newOption = {
      optionNumber: options.length + 1,
      optionText: "",
      answer: false,
    };

    addOption(activeSequenceData.sequenceNumber!, newOption);
  };

  return (
    <div>
      <p>질문답변 set</p>
      <div>
        <TextInput
          name="question"
          placeholder="질문을 입력하세요"
          value={activeSequenceData.question}
          onChange={(val) =>
            updateSequence(activeSequenceData.sequenceNumber!, {
              question: val,
            })
          }
        >
          질문
        </TextInput>
      </div>
      <div>
        <p>답변(최대 4개)</p>
        <div className="flex flex-wrap gap-4 justify-start">
          {options!.map((option, idx) => (
            <div
              key={option.optionNumber}
              className="w-[48%] min-w-[300px] border p-4 rounded-lg"
            >
              <div className="flex items-center gap-3">
                <input
                  type="radio"
                  name={`isAnswer-${activeSequenceData.sequenceNumber}`}
                  checked={option.answer}
                  onChange={() => {
                    // 정답 바꾸기
                    const updated = options.map((opt, i) => ({
                      ...opt,
                      answer: i === idx, // 해당 인덱스만 true
                    }));
                    updateSequence(activeSequenceData.sequenceNumber!, {
                      options: updated,
                    });
                  }}
                />
                <TextInput
                  name={`answer-${idx}`}
                  placeholder="정답을 입력하세요"
                  value={option.optionText}
                  onChange={(val) => handleOptionChange(idx, val)}
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
                + 추가하기
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
