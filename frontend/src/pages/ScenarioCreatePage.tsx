/** @format */

import { useState } from 'react';

import SequenceForm from '../components/create/SequenceForm';
import ScenarioForm from '../components/create/ScenarioForm';

export default function ScenarioCreatePage() {
  const [step, setStep] = useState(1);

  const circleClass = (n: number) =>
    n <= step ? 'bg-blue-500 text-white' : 'bg-gray-400 text-white';

  const textClass = (n: number) =>
    n <= step ? 'text-blue-500' : 'text-gray-500';

  const handleGotoNext = () => {
    setStep((prev) => prev + 1);
  };
  const handleGotoPrev = () => {
    setStep((prev) => prev - 1);
  };

  return (
    <div>
      <div className="flex flex-col items-center gap-6 p-6 mt-8">
        <div className="flex items-center gap-5">
          <div className="flex flex-col items-center gap-2">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center ${circleClass(
                1
              )}`}
            >
              1
            </div>
            <div className={`ml-2 whitespace-nowrap ${textClass(1)}`}>
              시나리오 정보
            </div>
          </div>

          <div className="mx-2 text-gray-400 text-xl"> {`>`}</div>

          <div className="flex flex-col items-center gap-2">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center ${circleClass(
                2
              )}`}
            >
              2
            </div>
            <div className={`ml-2 whitespace-nowrap ${textClass(2)}`}>
              질문답변 생성
            </div>
          </div>
        </div>

        {/* 실제 렌더링 */}
        <div className="w-full px-5 mt-5">
          {step == 1 && <ScenarioForm onNext={handleGotoNext} />}
          {step == 2 && <SequenceForm onPrev={handleGotoPrev} />}
        </div>
      </div>
    </div>
  );
}
