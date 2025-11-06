/**
 * 시나리오 난이도 선택하는 페이지
 * TODO: 새로고침 시 깜빡이는 현상 있음 -> asset 최적화하기
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
    <div className="relative min-h-screen flex items-center justify-center">
      <div
        className="absolute w-full h-screen bg-cover bg-center font-shark z-0"
        style={{ backgroundImage: `url(${backgroundImg})` }}
      ></div>
      <div className="relative z-10 w-full max-w-5xl px-4">
        <img src={blackboard} alt="칠판" className="w-full h-auto" />
        {scenario ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center z-10 text-slate-200">
            <p className="font-shark text-3xl mb-4">{scenario.title}</p>
            <p className="font-shark text-xl mb-8">
              학습 목표: {scenario.summary}
            </p>

            {/* TODO: Btns 렌더링 동적으로 변경 */}
            {/* TODO: Btn 공통 컴포넌트로 변경 */}
            <div className="flex gap-5 font-shark text-xl">
              <button
                onClick={() => handleStart("HARD")}
                className="bg-orange-500 hover:bg-orange-600 px-8 py-3 rounded-lg transition"
              >
                난이도 상
              </button>
              <button
                onClick={() => handleStart("NORMAL")}
                className="bg-yellow-500 hover:bg-yellow-600 px-8 py-3 rounded-lg transition"
              >
                난이도 중
              </button>
              <button
                onClick={() => handleStart("EASY")}
                className="bg-green-500 hover:bg-green-600 px-8 py-3 rounded-lg transition"
              >
                난이도 하
              </button>
            </div>
          </div>
        ) : (
          <p className="font-shark text-xl">에러 발생</p>
        )}
      </div>
    </div>
  );
}
