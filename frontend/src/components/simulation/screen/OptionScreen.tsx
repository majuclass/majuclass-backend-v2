/** @format */

import type { Sequence, TransformedOption } from "../../../types/Scenario";
import OptionButton from "../OptionButton";
import girlHead from "../../../assets/scenarios/cinema/cinema-girl-head.png";
import Record from "../audio/WebSocketTest";

type OptionScreenProps = {
  options: TransformedOption[];
  sequence: Sequence;
  onSelect: (option: TransformedOption) => void;
  sessionId?: number;
  sequenceNumber?: number;
  difficulty?: string;
};

export default function OptionScreen({
  options,
  sequence,
  onSelect,
  sessionId,
  sequenceNumber,
  difficulty,
}: OptionScreenProps) {
  const colors = ["pink", "yellow", "green", "blue"] as const; // 색상 순서 지정

  return (
    <div className="flex flex-col items-center min-h-screen p-6">
      {/* 난이도 "상"일 때만 녹음 버튼 표시 */}
      {difficulty === "HARD" && sessionId && (
        <div className="absolute bottom-48 z-30">
          <Record sessionId={sessionId} sequenceNumber={sequenceNumber ?? 1} />
        </div>
      )}
      <div className="flex items-center">
        {/* 머리머리 */}
        <img src={girlHead} alt="직원 머리" className="w-24 h-auto mb-2" />
        <div className="bg-white rounded-2xl px-6 py-3 text-2xl font-semibold shadow">
          {sequence.question}
        </div>
      </div>
      {/* 선택지 */}
      <div
        className={`flex flex-row gap-6 p-6 font-shark font-normal text-2xl flex-grow   
            `}
        // ${options.length === 3 ? "grid-cols-3" : ""}
      >
        {options.map((option, index) => {
          if (option.type === "image") {
            return (
              <div className="flex justify-center items-center">
                <OptionButton
                  key={option.id}
                  color={colors[index % colors.length]}
                  onClick={() => onSelect(option)}
                >
                  <div className="p-4 max-w-full max-h-full object-contain">
                    <img
                      src={option.label}
                      alt="옵션 미리보기"
                      className="max-w-full max-h-[300px] object-contain"
                    />
                  </div>
                </OptionButton>
              </div>
            );
          }

          if (option.type === "text") {
            return (
              <OptionButton
                key={option.id}
                color={colors[index % colors.length]}
                onClick={() => onSelect(option)}
              >
                {option.label}
              </OptionButton>
            );
          }
          return null;
        })}
      </div>
    </div>
  );
}
