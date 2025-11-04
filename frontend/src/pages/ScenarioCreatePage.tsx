/** @format */

import { useState } from "react";
import type { CreateScenario } from "../types/Scenario";

export default function ScenarioCreatePage() {
  const [step, setStep] = useState(1);
  const [datas, setDatas] = useState<CreateScenario | null>(null);

  return (
    <div>
      <p>current Step: {step}</p>
      {/* 제목 */}
      {/* 카테고리 */}
    </div>
  );
}
