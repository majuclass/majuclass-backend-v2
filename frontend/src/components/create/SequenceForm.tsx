/** @format */

<<<<<<< HEAD
import { useEffect, useState } from "react";
=======
import { useState } from "react";
import axios from "axios";
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import api from "../../apis/apiInstance";
import TextButton from "../TextButton";
import SequenceInput from "./SequenceInput";
import SequenceTabHeader from "./SequenceTabHeader";

// Types
interface SequenceFormProps {
  onPrev: () => void;
}

<<<<<<< HEAD
=======
interface CreateScenarioPayload {
  title: string;
  summary: string;
  categoryId: number;
  sequences: SequencePayload[];
  thumbnailS3Key?: string;
  backgroundS3Key?: string;
}

interface SequencePayload {
  seqNo: number;
  question: string;
  options: OptionPayload[];
}

interface OptionPayload {
  optionNo: number;
  optionText: string;
  optionS3Key: string;
  isAnswer: boolean;
}

// Component
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
export default function SequenceForm({ onPrev }: SequenceFormProps) {
  const sequences = useScenarioCreateStore((s) => s.sequences);
  const { addSequence, deleteSequence } = useScenarioCreateStore();
  const [activeSeqNum, setActiveSeqNum] = useState(1);

  // activeSeqNum 유효성 오류 해결 유효하지 않으면 첫 번째로 자동 이동
  useEffect(() => {
    const exists = sequences.some((s) => s.seqNo === activeSeqNum);
    if (!exists && sequences.length > 0) {
      setActiveSeqNum(sequences[0].seqNo!);
    }
  }, [sequences, activeSeqNum]);

  const activeSequence = sequences.find((s) => s.seqNo === activeSeqNum);

  // Validation 
  const validateScenarioData = (): boolean => {
    const { categoryId, sequences } = useScenarioCreateStore.getState();

    if (!categoryId || categoryId === 0) {
      alert("카테고리를 선택해주세요.");
<<<<<<< HEAD
      return;
=======
      return false;
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
    }

    for (let i = 0; i < sequences.length; i++) {
      const seq = sequences[i];

<<<<<<< HEAD
      // 질문 확인
      if (!seq.question || !seq.question.trim()) {
=======
      if (!seq.question?.trim()) {
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
        alert(`시퀀스 ${i + 1}번: 질문을 입력해주세요.`);
        return false;
      }

      for (let j = 0; j < seq.options.length; j++) {
        const opt = seq.options[j];

<<<<<<< HEAD
        if (!opt.optionText || !opt.optionText.trim()) {
          alert(
            `시퀀스 ${i + 1}번, 옵션 ${j + 1}번: 답변 텍스트를 입력해주세요.`
          );
          return;
=======
        if (!opt.optionText?.trim()) {
          alert(`시퀀스 ${i + 1}번, 옵션 ${j + 1}번: 답변 텍스트를 입력해주세요.`);
          return false;
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
        }

        if (!opt.optionS3Key) {
          alert(`시퀀스 ${i + 1}번, 옵션 ${j + 1}번: 아이콘을 선택해주세요.`);
          return false;
        }
      }
    }
<<<<<<< HEAD

    let thumbnailS3Key: string = "";
    let backgroundS3Key: string = "";
=======
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)

    return true;
  };

  // Image Upload 
  const uploadImageToS3 = async (
    file: File,
    imageType: string
  ): Promise<string> => {
    const { data } = await api.post("scenarios/image-upload-url", {
      imageType,
      contentType: file.type,
    });

    const { presignedUrl, s3Key } = data.data;

    const response = await axios.put(presignedUrl, file, {
      headers: { "Content-Type": file.type },
    });

    if (response.status === 200 || response.status === 204) {
      return s3Key;
    }

    throw new Error("Image upload failed");
  };

  const uploadImages = async (): Promise<{
    thumbnailS3Key: string;
    backgroundS3Key: string;
  }> => {
    const { thumbnail, background } = useScenarioCreateStore.getState();
    let thumbnailS3Key = "";
    let backgroundS3Key = "";

    if (thumbnail) {
      thumbnailS3Key = await uploadImageToS3(thumbnail, "THUMBNAIL");
      console.log("Thumbnail uploaded:", thumbnailS3Key);
    }

    if (background) {
      backgroundS3Key = await uploadImageToS3(background, "BACKGROUND");
      console.log("Background uploaded:", backgroundS3Key);
    }

    return { thumbnailS3Key, backgroundS3Key };
  };

  // Prepare Payload 
  const prepareScenarioPayload = (
    thumbnailS3Key: string,
    backgroundS3Key: string
  ): CreateScenarioPayload => {
    const { title, summary, categoryId, sequences } =
      useScenarioCreateStore.getState();

    const cleanedSequences: SequencePayload[] = sequences.map((seq, sIdx) => ({
      seqNo: seq.seqNo ?? sIdx + 1,
      question: seq.question,
      options: seq.options.map((opt, oIdx) => ({
        optionNo: opt.optionNo ?? oIdx + 1,
        optionText: opt.optionText,
        optionS3Key: opt.optionS3Key,
        isAnswer: !!opt.isAnswer,
      })),
    }));

    return {
      title,
      summary,
      categoryId,
      sequences: cleanedSequences,
      ...(thumbnailS3Key && { thumbnailS3Key }),
      ...(backgroundS3Key && { backgroundS3Key }),
    };
  };

  // Scenario 
  const handleCreateScenario = async (): Promise<void> => {
    if (!validateScenarioData()) return;

    try {
      const { thumbnailS3Key, backgroundS3Key } = await uploadImages();
      const scenarioData = prepareScenarioPayload(
        thumbnailS3Key,
        backgroundS3Key
      );

      console.log("[create-scenario] request payload:", scenarioData);

      const response = await api.post("scenarios/create", scenarioData);

      console.log("[create-scenario] response:", response.status, response.data);

      if (response.data.status === "SUCCESS") {
        alert("시나리오가 성공적으로 생성되었습니다.");
        console.log("생성 성공");
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error("[create-scenario] request failed", {
          status: error.response?.status,
          data: error.response?.data,
          url: error.config?.url,
          method: error.config?.method,
        });
        alert("시나리오 생성 중 오류가 발생했습니다.");
      } else {
        console.error(error);
      }
    }
  };

<<<<<<< HEAD
  const handleDeleteSequence = (seqNo: number) => {
    if (sequences.length <= 1) {
      alert("질문답변은 최소 1개 필요합니다.");
      return;
    }
    deleteSequence(seqNo);
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

=======
  // Render
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
  return (
    <>
      {/* Sequence Tabs Header */}
      <div className="flex flex-row flex-wrap items-center gap-2 rounded-2xl p-3">
        {sequences.map((sequence) => (
<<<<<<< HEAD
          <div key={sequence.seqNo}>
            <SequenceTabHeader
              num={sequence.seqNo}
              isActive={activeSeqNum === sequence.seqNo}
              onClick={() => setActiveSeqNum(sequence.seqNo!)}
              onDelete={() => handleDeleteSequence(sequence.seqNo!)}
            />
          </div>
=======
          <SequenceTabHeader
            key={sequence.seqNo}
            num={sequence.seqNo}
            isActive={activeSeqNum === sequence.seqNo}
            onClick={() => setActiveSeqNum(sequence.seqNo!)}
            onDelete={() => deleteSequence(sequence.seqNo!)}
          />
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
        ))}
        <button
          onClick={addSequence}
          className="ml-2 px-3 py-1 rounded-full bg-pink-100 text-pink-600 border border-pink-300 hover:bg-pink-200 transition-all text-sm"
        >
          추가하기
        </button>
      </div>

<<<<<<< HEAD
      {/* 시퀀스 */}
      <div>
        {/* 시퀀스 생성 */}
        <div>
          <div className="flex flex-row items-center gap-2 mb-4 bg-pink-50 rounded-2xl justify-center min-h-[600px] transition-all duration-300">
            {activeSequence && (
              <SequenceInput activeSeq={activeSequence!}></SequenceInput>
            )}
          </div>
        </div>
        {/* 버튼 */}
        <div className="flex flex-row justify-between">
          <TextButton onClick={onPrev}>이전</TextButton>
          <TextButton onClick={handleCreateScenario}>저장</TextButton>
        </div>
=======
      {/* Sequence Input */}
      <div className="flex flex-row items-center gap-2 mb-4 bg-pink-50 rounded-2xl justify-center min-h-[600px] transition-all duration-300">
        <SequenceInput activeSeq={activeSequence!} />
      </div>

      {/* Navigation Buttons */}
      <div className="flex flex-row justify-between">
        <TextButton onClick={onPrev}>이전</TextButton>
        <TextButton onClick={handleCreateScenario}>저장</TextButton>
>>>>>>> 43f1927 ([FE] Feature: 대시보드 1차 완료)
      </div>
    </>
  );
}