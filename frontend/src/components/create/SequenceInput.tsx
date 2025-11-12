/** @format */
import TextInput from "../TextInput";
import {
  useScenarioCreateStore,
  type SequenceData,
} from "../../stores/useScenarioCreateStore";
import boyHead from "../../assets/scenarios/cinema/cinema-boy-head.png";
import axios from "axios";
import api from "../../apis/apiInstance";
import PictogramPopover from "../PictogramPopover"; // 픽토그램 추가
import type { PictogramItem } from "../PictogramPopover"; // type import 분리
import { useRef, useState } from "react"; // react 추가

interface SequenceInputProps {
  activeSeq: SequenceData;
}

export default function SequenceInput({ activeSeq }: SequenceInputProps) {
  // 현재 sequence
  const activeSequenceData = useScenarioCreateStore((s) =>
    s.sequences.find((seq) => seq.seqNo === activeSeq.seqNo)
  );

  const addOption = useScenarioCreateStore((s) => s.addOption);
  const deleteOption = useScenarioCreateStore((s) => s.deleteOption);
  const updateSequence = useScenarioCreateStore((s) => s.updateSequence);

  // 팝오버 상태 관리 추가
  const [openPopoverIndex, setOpenPopoverIndex] = useState<number | null>(null);
  const buttonRefs = useRef<{ [key: number]: HTMLButtonElement | null }>({});

  if (!activeSequenceData) return null;

  const options = activeSequenceData.options;

  // 이벤트 핸들러

  const handleOptionChange = (idx: number, text: string) => {
    const newOptions = [...options];
    newOptions[idx] = { ...newOptions[idx], optionText: text };

    updateSequence(activeSequenceData.seqNo!, {
      options: newOptions,
    });
  };

  // Pictogram 선택 핸들러 추가
  const handlePictogramSelect = async (idx: number, item: PictogramItem) => {
    console.log("선택된 아이콘:", item);

    // 1. 미리보기 즉시 표시
    const updated = options.map((opt, i) =>
      i === idx ? { ...opt, optionIconUrl: item.src } : opt
    );
    updateSequence(activeSequenceData.seqNo!, { options: updated });

    // 2. 모든 경우에 S3 업로드 (blob이든 정적 이미지든)
    try {
      let file: File;

      if (item.src.startsWith("blob:")) {
        // blob URL인 경우
        const response = await fetch(item.src);
        const blob = await response.blob();
        file = new File([blob], item.name, { type: blob.type });
      } else {
        // 정적 이미지도 fetch해서 File로 변환
        const response = await fetch(item.src);
        const blob = await response.blob();
        file = new File([blob], item.name || "icon.png", {
          type: blob.type || "image/png",
        });
      }

      const s3Key = await handleUpload(file, "OPTION");

      // 3. S3 키 업데이트
      // const finalUpdated = options.map((opt, i) =>
      //   i === idx ? { ...opt, optionS3Key: s3Key } : opt
      // );

      const currentSequence = useScenarioCreateStore
        .getState()
        .sequences.find((s) => s.seqNo === activeSequenceData.seqNo);

      const finalUpdated = currentSequence!.options.map((opt, i) =>
        i === idx ? { ...opt, optionS3Key: s3Key } : opt
      );

      updateSequence(activeSequenceData.seqNo!, { options: finalUpdated });
      console.log("S3 업로드 완료:", s3Key);
    } catch (error) {
      console.error("S3 업로드 실패:", error);
      alert("이미지 업로드에 실패했습니다.");
    }

    setOpenPopoverIndex(null);
  };

  const handleAddOption = () => {
    if (options.length >= 4) return;

    const newOption = {
      optionNo: options.length + 1,
      optionText: "",
      optionS3Key: "",
      isAnswer: false,
    };

    addOption(activeSequenceData.seqNo!, newOption);
  };

  const handleUpload = async (file: File, imageType: string) => {
    // presignedurl 요청
    try {
      const { data } = await api.post("scenarios/image-upload-url", {
        imageType: imageType,
        contentType: file.type,
      });
      console.log(data);

      const { presignedUrl, s3Key } = data.data;

      // s3 업로드
      const resp = await axios.put(presignedUrl, file, {
        headers: { "Content-Type": file.type },
      });

      if (resp.status === 200 || resp.status === 204) return s3Key;
    } catch (error) {
      console.error(error);
    }
  };

  const handleRemoveOption = (optNo: number) => {
    if (options.length <= 1) {
      alert("최소 1개의 답변은 필요합니다.");
      return;
    }
    deleteOption(activeSequenceData.seqNo!, optNo);
  };
  // // 위의 함수와 중복
  // const handleIconUpload = async (
  //   e: React.ChangeEvent<HTMLInputElement>,
  //   idx: number
  // ) => {
  //   const file = e.target.files?.[0];
  //   if (!file) return;

  //   const previewUrl = URL.createObjectURL(file);

  //   const updated = options.map((opt, i) =>
  //     i === idx ? { ...opt, previewUrl } : opt
  //   );

  //   updateSequence(activeSequenceData.seqNo!, {
  //     options: updated,
  //   });

  //   // s3 업로드 비동기로 시도
  //   try {
  //     const s3Key = await handleUpload(file, "OPTION");

  //     const cleanedUpdated = options.map((opt, i) =>
  //       i === idx
  //         ? {
  //             optionNo: opt.optionNo,
  //             optionText: opt.optionText,
  //             optionS3Key: s3Key,
  //             isAnswer: opt.isAnswer,
  //             //   previewUrl, // UI 표시용 (보내지는 않음)
  //           }
  //         : opt
  //     );

  //     updateSequence(activeSequenceData.seqNo!, { options: cleanedUpdated });
  //     console.log("업로드 완료: " + options);
  //     console.log("업로드 완료" + s3Key);
  //   } catch (error) {
  //     console.error(error);
  //   }
  // };

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
            updateSequence(activeSequenceData.seqNo!, {
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
              key={option.optionNo}
              className="border p-4 rounded-lg bg-pink-200 flex items-center gap-3"
            >
              {/* 정답 묶음 */}
              <div className="flex items-center gap-3">
                <input
                  type="radio"
                  name={`isAnswer-${activeSequenceData.seqNo}`}
                  checked={option.isAnswer}
                  onChange={() => {
                    // 정답 바꾸기
                    const updated = options.map((opt, i) => ({
                      ...opt,
                      isAnswer: i === idx, // 해당 인덱스만 true
                    }));
                    updateSequence(activeSequenceData.seqNo!, {
                      options: updated,
                    });
                  }}
                  className="accent-pink-400"
                />

                {/* 아이콘 미리보기 */}
                <div className="flex items-center justify-center w-32 h-32 bg-white rounded-xl border border-gray-200 overflow-hidden">
                  {option.optionIconUrl ? ( // icon -> optionIconUrl으로 수정
                    <img
                      src={option.optionIconUrl} // icon -> optionIconUrl으로 수정
                      alt="아이콘 미리보기"
                      className="object-contain w-full h-full"
                    />
                  ) : (
                    <span className="text-gray-400 text-sm">이미지 없음</span>
                  )}
                </div>

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
                  {/* 임시 삭제 */}
                  {/* 파일 업로드 */}
                  {/* <label className="flex items-center justify-between px-3 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-pink-50 transition">
                    <span className="font-medium">아이콘</span>
                    <input
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(e) => handleIconUpload(e, idx)}
                    />
                    <span className="text-sm text-gray-500">파일 선택</span>
                  </label> */}
                  {/* 임시 삭제 */}

                  {/* 팝오버 트리거 버튼 추가*/}
                  <button
                    ref={(el) => {
                      buttonRefs.current[idx] = el; // ref 수정
                    }}
                    onClick={() => setOpenPopoverIndex(idx)}
                    className="flex items-center justify-between px-3 py-2 border border-gray-300 rounded-lg hover:bg-pink-50 transition"
                  >
                    <span className="font-medium">아이콘</span>
                    <span className="text-sm text-gray-500">파일 선택</span>
                  </button>

                  {/* Pictogram Popover */}
                  <PictogramPopover
                    open={openPopoverIndex === idx}
                    onClose={() => setOpenPopoverIndex(null)}
                    onSelect={(item) => handlePictogramSelect(idx, item)}
                    anchorRef={{ current: buttonRefs.current[idx] || null }} // || null 추가
                  />

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

              {/* 삭제 버튼 */}

              <button
                onClick={() => handleRemoveOption(option.optionNo!)}
                className="w-8 h-8 bg-red-500 text-white rounded-full hover:bg-red-600 transition flex items-center justify-center text-lg font-bold flex-shrink-0"
                title="답변 삭제"
              >
                X
              </button>
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
