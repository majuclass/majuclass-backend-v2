/**
 * 시나리오 난이도 선택하는 페이지
 * @format
 */

import blackboard from "../assets/scenarios/common/blackboard.png";
import backgroundImg from "../assets/scenarios/cinema/cinema-ticket-bg-img.png";
import api from "../apis/apiInstance";
import { useEffect, useState } from "react";
import type { Scenario } from "../types/Scenario";
import { useNavigate, useParams } from "react-router-dom";

export default function SelectLevelPage() {
  const [scenario, setScenario] = useState<Scenario | null>(null);
  const navigate = useNavigate();
  const { scenarioId } = useParams();

  const fetchScenario = async () => {
    try {
      const resp = await api.get(`scenarios/${scenarioId}`);
      const data = resp.data.data;
      setScenario(data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    fetchScenario();
  });

  const handleStart = (difficulty: string) => {
    if (scenario) {
      navigate(`/simulation/${scenarioId}/${difficulty}`);
    }
  };

  return (
    <div>
      {scenario && (
        <div>
          <p>{scenario.title}</p>
          <p>{scenario.summary}</p>
          <p>종류: {scenario.categoryName}</p>
          {/* TODO: 렌더링 동적으로 변경 */}
          <button onClick={() => handleStart("HARD")}>난이도 상</button>
          <button onClick={() => handleStart("NORMAL")}>난이도 중</button>
          <button onClick={() => handleStart("EASY")}>난이도 하</button>
        </div>
      )}
    </div>
  );
}
