/** @format */

import { useEffect, useState } from "react";
import type { Option, Scenario, Sequence } from "../types/Scenario";
import api from "../apiInstance";
import OptionScreen from "../components/scenario/OptionScreen";
import StartScreen from "../components/scenario/StartScreen";
import SequenceScreen from "../components/scenario/SequenceScreen";
import EndScreen from "../components/scenario/EndScreen";
import FeedbackScreen from "../components/scenario/FeedbackScreen";

export default function SimulationPage() {
  const [gameState, setGameState] = useState<"loading" | "error" | "playing">(
    "loading"
  );
  const [screen, setScreen] = useState<
    "start" | "question" | "option" | "feedback" | "end"
  >("start");
  const [scenario, setScenario] = useState<Scenario>();
  const [sequence, setSequence] = useState<Sequence>();
  const [options, setOptions] = useState<Option[]>([]);
  const [sequenceNumber, setSequenceNumber] = useState(1);
  const [isCorrect, setIsCorrect] = useState<boolean>(false);

  //   TODO: id 분리
  const scenarioId = 1;

  // 시나리오는 최초 로딩 1회
  useEffect(() => {
    const fetchScenario = async () => {
      try {
        const resp = await api.get(`scenarios/${scenarioId}`);
        const data = resp.data.data;
        setScenario(data);
      } catch (error) {
        console.error(error);
        setGameState("error");
      }
    };
    fetchScenario();
  }, [scenarioId]);

  // 1 시퀀스 먼저 불러오기
  useEffect(() => {
    if (sequenceNumber === 0) return;
    const fetchSequence = async () => {
      setGameState("loading");
      try {
        const resp = await api.get(
          `scenario-sessions/${scenarioId}/sequences/${sequenceNumber}`
        );
        const data = resp.data.data;
        setSequence(data);
        setGameState("playing");
      } catch (error) {
        console.error(error);
        setGameState("error");
      }
    };
    fetchSequence();
  }, [scenarioId, sequenceNumber]);

  // 2. 각 시퀀스의 옵션 불러오기
  useEffect(() => {
    if (!sequence) return;
    const fetchOptions = async () => {
      try {
        const resp = await api.get(
          `scenario-sessions/${scenarioId}/sequences/${sequenceNumber}/options`
        );
        setOptions(resp.data.data);
      } catch (error) {
        console.error(error);
        setGameState("error");
      }
    };
    fetchOptions();
  }, [sequence, scenarioId, sequenceNumber]);

  // 이벤트 핸들러: 시작 / 옵션 선택 / 다음 문제 / 초기화
  const handleGameStart = () => {
    setGameState("playing");
    setScreen("question");
    setIsCorrect(false);
  };
  // 옵션 선택 화면으로 전환
  const handleSelectOption = () => {
    setScreen("option");
    setIsCorrect(false);
  };

  // 피드백 처리 & 다음 시퀀스로 이동
  const handleFeedback = async (selectedOption: Option) => {
    try {
      const resp = await api.post(`scenario-sessions/submit-answer`, {
        scenarioId,
        sequenceNumber,
        selectedOptionId: selectedOption.optionId,
      });

      const { correct } = resp.data.data;

      setIsCorrect(correct);
      setScreen("feedback");

      setTimeout(() => {
        if (!correct) {
          setScreen("option");
          return;
        }

        // 정답인 경우
        const isLastSequence =
          sequenceNumber >= (scenario?.totalSequences || 0);

        if (isLastSequence) {
          setScreen("end");
        } else {
          setSequenceNumber((prev) => prev + 1);
          setScreen("question");
        }
      }, 2000);
    } catch (error) {
      console.error(error);
      setGameState("error");
    }
  };

  const handleRestart = () => {
    setGameState("loading");
    setScreen("start");
    setSequenceNumber(1);
    setIsCorrect(false);
  };

  // 화면 렌더링 조건 분기
  const renderContent = () => {
    // 시나리오별
    switch (gameState) {
      case "loading":
        return <div>로딩 중 ...</div>;
      case "error":
        return <div>에러 발생</div>;
      case "playing":
        switch (screen) {
          case "start":
            return scenario ? (
              <StartScreen scenario={scenario} onStart={handleGameStart} />
            ) : null;
          case "question":
            return sequence ? (
              <SequenceScreen sequence={sequence} onNext={handleSelectOption} />
            ) : null;
          case "option":
            return options ? (
              <OptionScreen options={options} onSelect={handleFeedback} />
            ) : null;
          case "feedback":
            return <FeedbackScreen isCorrect={isCorrect} />;
          case "end":
            return <EndScreen onRestart={handleRestart} />;
        }
    }
  };
  //   실 렌더링
  return (
    <div className="scenario-player">
      <h1>{scenario?.title}</h1>
      <div className="content">
        {/* TODO: progress bar */}
        {renderContent()}
      </div>
    </div>
  );
}
