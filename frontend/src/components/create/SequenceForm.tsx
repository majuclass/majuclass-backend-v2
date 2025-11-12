/** @format */

import { useEffect, useState } from "react";
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

  // activeSeqNum 유효성 오류 해결 유효하지 않으면 첫 번째로 자동 이동
  useEffect(() => {
    const exists = sequences.some((s) => s.seqNo === activeSeqNum);
    if (!exists && sequences.length > 0) {
      setActiveSeqNum(sequences[0].seqNo!);
    }
  }, [sequences, activeSeqNum]);

  const activeSequence = sequences.find((s) => s.seqNo === activeSeqNum);

  const handleCreateScenario = async () => {
    const { title, summary, thumbnail, background, categoryId, sequences } =
      useScenarioCreateStore.getState();

    // 구체적인 검증 로직
    // 1. 카테고리 확인
    if (!categoryId || categoryId === 0) {
      alert("카테고리를 선택해주세요.");
      return;
    }

    // 2. 각 시퀀스별 검증
    for (let i = 0; i < sequences.length; i++) {
      const seq = sequences[i];

      // 질문 확인
      if (!seq.question || !seq.question.trim()) {
        alert(`시퀀스 ${i + 1}번: 질문을 입력해주세요.`);
        return;
      }

      // 옵션 확인
      for (let j = 0; j < seq.options.length; j++) {
        const opt = seq.options[j];

        if (!opt.optionText || !opt.optionText.trim()) {
          alert(
            `시퀀스 ${i + 1}번, 옵션 ${j + 1}번: 답변 텍스트를 입력해주세요.`
          );
          return;
        }

        if (!opt.optionS3Key) {
          alert(`시퀀스 ${i + 1}번, 옵션 ${j + 1}번: 아이콘을 선택해주세요.`);
          return;
        }
      }
    }

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
    // sequences 데이터 정체 추가
    // 변경: API 스키마에 맞게 필드명 매핑
    // - sequenceNumber/optionNumber 로 변환
    // - optionText/optionS3Key는 값이 있을 때만 포함
    const cleanedSequences = sequences.map((seq, sIdx) => ({
      seqNo: seq.seqNo ?? sIdx + 1,
      question: seq.question,
      options: seq.options.map((opt, oIdx) => ({
        optionNo: opt.optionNo ?? oIdx + 1,
        optionText: opt.optionText,
        optionS3Key: opt.optionS3Key,
        isAnswer: !!opt.isAnswer,
      })),
    }));

    // 변경: 최상위 필드 보강 및 선택 필드 조건 포함
    // - totalSequences 추가
    // - difficulty 기본값 추가 (TODO: UI에서 선택값으로 대체)
    // - thumbnail/background 키는 값이 있을 때만 전송
    // any 사용 금지: 명시적 타입 정의 추가
    type CreateScenarioPayload = {
      title: string;
      summary: string;
      categoryId: number;
      // difficulty: "EASY" | "NORMAL" | "HARD";
      // totalSequences: number;
      sequences: {
        seqNo: number;
        question: string;
        options: {
          optionNo: number;
          isAnswer: boolean;
          optionText: string;
          optionS3Key: string;
        }[];
      }[];
      thumbnailS3Key?: string;
      backgroundS3Key?: string;
    };
    const scenarioData: CreateScenarioPayload = {
      title,
      summary,
      categoryId,
      // difficulty: "EASY", // TODO: ScenarioForm에서 난이도 선택값 반영
      // totalSequences: cleanedSequences.length,
      sequences: cleanedSequences,
      ...(thumbnailS3Key && { thumbnailS3Key }), // 조건부 프로퍼티
      ...(backgroundS3Key && { backgroundS3Key }), // 조건부 프로퍼티
    };
    // if (thumbnailS3Key) scenarioData.thumbnailS3Key = thumbnailS3Key;
    // if (backgroundS3Key) scenarioData.backgroundS3Key = backgroundS3Key;

    // const json = JSON.stringify(scenarioData);
    // console.log(json);
    console.log(scenarioData);

    // 생성 post
    try {
      const resp = await api.post(`scenarios/create`, scenarioData);
      // 변경: 요청/응답 로깅 추가
      console.log("[create-scenario] request payload:", scenarioData);
      console.log("[create-scenario] response:", resp.status, resp.data);
      if (resp.data.status === "SUCCESS") {
        console.log("생성 성공");
      }
    } catch (error) {
      // 변경: 에러 상세 로그 (상태/본문/URL)
      if (axios.isAxiosError(error)) {
        console.error("[create-scenario] request failed", {
          status: error.response?.status,
          data: error.response?.data,
          url: error.config?.url,
          method: error.config?.method,
        });
      } else {
        console.error(error);
      }
    }
  };

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
              onDelete={() => handleDeleteSequence(sequence.seqNo!)}
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
      </div>
    </>
  );
}
