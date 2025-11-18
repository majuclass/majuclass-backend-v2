/** @format */

import type { Sequence, TransformedOption } from '../../../types/Scenario';
import OptionButton from '../OptionButton';
import girlHead from '../../../assets/scenarios/cinema/cinema-girl-head.png';
import Record, { type STTResponse } from '../audio/WebSocketTest';

type OptionScreenProps = {
  options: TransformedOption[];
  sequence: Sequence;
  onSelect: (option: TransformedOption) => void;
  onSkip: () => void;
  sessionId?: number;
  sequenceNumber?: number;
  difficulty?: string;
  onSTTResult?: (res: STTResponse) => void;
};

export default function OptionScreen({
  options,
  sequence,
  onSelect,
  onSkip,
  sessionId,
  sequenceNumber,
  difficulty,
  onSTTResult,
}: OptionScreenProps) {
  const colors = ['pink', 'yellow', 'green', 'blue'] as const; // 색상 순서 지정

  return (
    <div className="flex flex-col items-center min-h-screen p-6">
      <button onClick={onSkip} className="text-2xl">
        {'>> 스킵하기'}
      </button>
      {/* 난이도 "상"일 때만 녹음 버튼 표시 - 우측 하단 고정 */}
      {difficulty === 'HARD' && sessionId && (
        <div className="fixed bottom-8 right-8 z-30">
          <Record
            sessionId={sessionId}
            sequenceNumber={sequenceNumber ?? 1}
            onSTTResult={onSTTResult}
          />
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
        className={`flex flex-row flex-wrap gap-6 p-6 font-shark font-normal text-2xl flex-grow justify-center
            `}
        // ${options.length === 3 ? "grid-cols-3" : ""}
      >
        {options.map((option, index) => {
          if (option.type === 'image') {
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

          if (option.type === 'text') {
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
