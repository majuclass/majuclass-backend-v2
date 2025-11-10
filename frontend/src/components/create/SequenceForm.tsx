/** @format */

import { useState } from "react";
import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import TextButton from "../TextButton";
import SequenceInput from "./SequenceInput";
import SequenceTabHeader from "./SequenceTabHeader";
import api from "../../apis/apiInstance";
import axios from "axios";

interface SequenceFormProps {
  onPrev: () => void;
}

export default function SequenceForm({ onPrev }: SequenceFormProps) {
  const sequences = useScenarioCreateStore((s) => s.sequences);
  const { addSequence } = useScenarioCreateStore();
  const { deleteSequence } = useScenarioCreateStore();

  //   현재 활성 시퀀스
  const [activeSeqNum, setActiveSeqNum] = useState(1);

  const activeSequence = sequences.find((s) => s.seqNo === activeSeqNum);

  const handleCreateScenario = async () => {
    const { title, summary, thumbnail, background, categoryId, sequences } =
      useScenarioCreateStore.getState();

    let thumbnailS3Key: string = "";
    let backgroundS3Key: string = "";

    try {
      if (thumbnail) {
        thumbnailS3Key = (await handleUpload(thumbnail, "THUMBNAIL")) || "";
        console.log("thumbnailkey:" + thumbnailS3Key);
      }
      if (background) {
        backgroundS3Key = (await handleUpload(background, "BACKGROUND")) || "";
        console.log("bgkey: " + backgroundS3Key);
      }
    } catch (error) {
      console.error(error);
    }

    const scenarioData = {
      title: title,
      summary: summary,
      categoryId: categoryId,
      thumbnailS3Key: thumbnailS3Key,
      backgroundS3Key: backgroundS3Key,
      sequences: sequences,
    };

    // const json = JSON.stringify(scenarioData);
    // console.log(json);
    console.log(scenarioData);

    // 생성 post
    try {
      const resp = await api.post(`scenarios/create`, scenarioData);
      if (resp.data.status === "SUCCESS") {
        console.log("생성 성공");
      }
    } catch (error) {
      console.log(error);
    }
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

  return (
    <>
      {/* TODO: 개수 제한  */}
      {/* Header */}
      <div className="flex flex-row flex-wrap items-center gap-2 rounded-2xl p-3">
        {sequences.map((sequence) => (
          <div key={sequence.seqNo}>
            <SequenceTabHeader
              num={sequence.seqNo}
              isActive={activeSeqNum === sequence.seqNo}
              onClick={() => setActiveSeqNum(sequence.seqNo!)}
              onDelete={() => deleteSequence(sequence.seqNo!)}
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
