/** @format */

import { useState } from "react";

import SequenceForm from "../components/create/SequenceForm";
import ScenarioForm from "../components/create/ScenarioForm";

export default function ScenarioCreatePage() {
  const [step, setStep] = useState(1);
  //   const [scenarioData, setScenarioData] = useState<CreateScenario | null>(null);

  const handleGotoNext = () => {
    setStep((prev) => prev + 1);
  };
  const handleGotoPrev = () => {
    setStep((prev) => prev - 1);
  };

  return (
    <div>
      {/* TODO: visualize to Progress bar */}
      <p>current Step: {step}</p>
      {/* 실제 렌더링 */}
      {step == 1 && <ScenarioForm onNext={handleGotoNext} />}
      {step == 2 && <SequenceForm onPrev={handleGotoPrev} />}
    </div>
  );
}
