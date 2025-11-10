/** @format */
import TextInput from "../TextInput";
import {
  useScenarioCreateStore,
  type SequenceData,
} from "../../stores/useScenarioCreateStore";
import boyHead from "../../assets/scenarios/cinema/cinema-boy-head.png";

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

  const handleIconUpload = (
    e: React.ChangeEvent<HTMLInputElement>,
    idx: number
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const previewUrl = URL.createObjectURL(file);

    const updated = options.map((opt, i) =>
      i === idx ? { ...opt, icon: previewUrl, iconFile: file } : opt
    );

    updateSequence(activeSequenceData.sequenceNumber!, {
      options: updated,
    });
  };

  return (
    <div className="flex flex-col bg-pink-50 rounded-2xl p-6 shadow-sm">
      <div className="flex items-center gap-3 mb-6 justify-center">
        <img
          src={boyHead}
          alt="캐릭터"
          className="w-10 h-10 object-contain flex-shrink-0"
        />
        <TextInput
          name="question"
          placeholder="어떻게 질문할까요?"
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

      {/* 답변 */}
      <div>
        <h3 className="font-semibold mb-3">답변 만들기(최대 4개)</h3>
        {/* 옵션 란 */}
        <div className="flex flex-wrap gap-4 justify-start">
          {options!.map((option, idx) => (
            <div
              key={option.optionNumber}
              className="border p-4 rounded-lg bg-pink-200"
            >
              {/* 정답 묶음 */}
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
                  className="accent-pink-400"
                />

                {/* 아이콘 미리보기 */}
                {/* <div className="flex items-center justify-center w-32 h-32 bg-white rounded-xl border border-gray-200 overflow-hidden">
                  {option.icon ? (
                    <img
                      src={option.icon}
                      alt="아이콘 미리보기"
                      className="object-contain w-full h-full"
                    />
                  ) : (
                    <span className="text-gray-400 text-sm">이미지 없음</span>
                  )}
                </div> */}

                {/* TODO: option pic 미리보기 전역에 추가 */}
                {/* <div className="flex items-center justify-center w-32 h-32 bg-white rounded-xl border border-gray-200 overflow-hidden">
                  {option.optionPic ? (
                    <img
                      src={option.optionPic}
                      alt="아이콘 미리보기"
                      className="object-contain w-full h-full"
                    />
                  ) : (
                    <span className="text-gray-400 text-sm">이미지 없음</span>
                  )}
                </div> */}

                {/* 아이콘 업로드 + 답변 입력 */}
                <div className="flex flex-col flex-1 gap-2 bg-white rounded-xl p-3 border border-gray-200">
                  {/* 파일 업로드 */}
                  <label className="flex items-center justify-between px-3 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-pink-50 transition">
                    <span className="font-medium">아이콘</span>
                    <input
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(e) => handleIconUpload(e, idx)}
                    />
                    <span className="text-sm text-gray-500">파일 선택</span>
                  </label>

                  {/* 답변 입력 */}
                  <div className="flex items-center justify-between px-3 py-2 border border-gray-300 rounded-lg">
                    <span className="font-medium">답변</span>
                    <input
                      type="text"
                      placeholder="답변을 입력하세요"
                      value={option.optionText}
                      onChange={(e) => handleOptionChange(idx, e.target.value)}
                      className="flex-1 ml-2 outline-none"
                    />
                  </div>
                </div>
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
