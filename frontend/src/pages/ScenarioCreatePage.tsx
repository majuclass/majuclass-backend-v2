/** @format */

import { useState } from "react";
import type { CreateScenario } from "../types/Scenario";
import ScenarioForm from "../components/create/ScenarioForm";
import SequenceForm from "../components/create/SequenceForm";

export default function ScenarioCreatePage() {
  const [step, setStep] = useState(1);
  const [scenarioData, setScenarioData] = useState<CreateScenario | null>(null);

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
      {step == 1 && (
        <ScenarioForm scenarioData={scenarioData} onNext={handleGotoNext} />
      )}
      {step == 2 && <SequenceForm onPrev={handleGotoPrev} />}
    </div>
  );
}
