/** @format */

import { useEffect, useState } from "react";
import { useParams } from "react-router-dom"; // 라우팅 연결 추가
import type { Option, Scenario, Sequence } from "../types/Scenario";
import api from "../apis/apiInstance";
import OptionScreen from "../components/scenario/screen/OptionScreen";
import StartScreen from "../components/scenario/screen/StartScreen";
import SequenceScreen from "../components/scenario/screen/SequenceScreen";
import EndScreen from "../components/scenario/screen/EndScreen";
import FeedbackScreen from "../components/scenario/screen/FeedbackScreen";
import ScenarioLayout from "../components/layout/ScenarioLayout";

// 이미지 추가 
import bgCinema from "../assets/scenarios/cinema/cinema-ticket-bg-img.png";
import girlNormal from "../assets/scenarios/cinema/cinema-girl-normal.png";


/** 시뮬레이션 실행 제어 컨트롤러
 * @param scenarioId - 불러올 시나리오 고유 ID
 */
export default function SimulationPage() {
  const { scenarioId: scenarioIdParam } = useParams<{ scenarioId: string }>(); // 연결 변수 
  const scenarioId = scenarioIdParam ? Number(scenarioIdParam) : 1; // 변경: 없으면 1로 fallback


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

  // TODO: 에러 처리
  //   TODO: id 분리
  // const scenarioId = 1;

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
            return options && sequence ? (
              <OptionScreen
                options={options}
                sequence={sequence}
                onSelect={handleFeedback}
              />
            ) : null;
          case "feedback":
            return <FeedbackScreen isCorrect={isCorrect} />;
          case "end":
            return <EndScreen onRestart={handleRestart} />;
        }
    }
  };
  //   실 렌더링
  //   TODO: img 확장
  return (
    <ScenarioLayout
      backgroundImg={
        // scenario?.backgroundImage ||
        bgCinema
      }
      characterImg={
        // scenario?.characterImage ||
        girlNormal
      }
      showCharacter={screen !== "start" && screen !== "option"}
      blurBackground={screen === "start" || screen === "option"}
    >
      {renderContent()}
    </ScenarioLayout>
  );
}
