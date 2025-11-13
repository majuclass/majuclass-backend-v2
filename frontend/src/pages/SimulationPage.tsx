/** @format */
import api from '../apis/apiInstance';
import type {
  GetScenario,
  Sequence,
  TransformedOption,
} from '../types/Scenario';
import ScenarioLayout from '../components/wrappers/ScenarioLayout';
import StartScreen from '../components/simulation/screen/StartScreen';
import SequenceScreen from '../components/simulation/screen/SequenceScreen';
import OptionScreen from '../components/simulation/screen/OptionScreen';
import EndScreen from '../components/simulation/screen/EndScreen';
import FeedbackScreen from '../components/simulation/screen/FeedbackScreen';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom'; // 라우팅 연결 추가

import bgCinema from '../assets/scenarios/cinema/cinema-ticket-bg-img.png';
import girlNormal from '../assets/scenarios/cinema/cinema-girl-normal.png';
import { transformOptions } from '../utils/format';

import { useUserStore } from "../stores/useUserStore";

/** 시뮬레이션 실행 제어 컨트롤러
 * @param scenarioId - 불러올 시나리오 고유 ID
 */
export default function SimulationPage() {
  const { scenarioId, difficulty } = useParams();
  

  //   시나리오 인터페이스 확장 위해 type alias 사용
  // TODO: 차후 확장 추가
  type ScenariowithURL = GetScenario & {
    backgroundUrl: string;
  };

  const [gameState, setGameState] = useState<
    'loading' | 'error' | 'playing' | 'end'
  >('loading');
  const [screen, setScreen] = useState<
    'start' | 'question' | 'option' | 'feedback' | 'end'
  >('start');
  const [scenario, setScenario] = useState<ScenariowithURL>();
  const [sequence, setSequence] = useState<Sequence>();
  const [options, setOptions] = useState<TransformedOption[]>([]);
  const [sequenceNumber, setSequenceNumber] = useState(1);
  const [isCorrect, setIsCorrect] = useState<boolean>(false);
  const [sessionId, setSessionId] = useState<number>();

  const studentId = useUserStore((s) => s.studentId);
  console.log("🔥 전역 studentId:", studentId);


  // 시나리오는 최초 로딩 1회
  useEffect(() => {
    const fetchScenario = async () => {
      try {
        const resp = await api.get(`scenarios/${scenarioId}`);
        const data = resp.data.data;
        setScenario(data);
      } catch (error) {
        console.error(error);
        setGameState('error');
      }
    };
    fetchScenario();
  }, [scenarioId]);

  // 1 시퀀스 먼저 불러오기
  useEffect(() => {
    if (sequenceNumber === 0) return;
    const fetchSequence = async () => {
      setGameState('loading');
      try {
        const resp = await api.get(
          `scenarios/${scenarioId}/sequences/${sequenceNumber}`
        );
        const data = resp.data.data;
        setSequence(data);
        setGameState('playing');
      } catch (error) {
        console.error(error);
        setGameState('error');
      }
    };
    fetchSequence();
  }, [scenarioId, sequenceNumber]);

  // 2. 각 시퀀스의 옵션 불러오기
  useEffect(() => {
    if (!sequence) return;
    const fetchOptions = async () => {
      try {
        if (!difficulty) return;

        const resp = await api.get(
          `scenarios/${scenarioId}/sequences/${sequenceNumber}/options?difficulty=${difficulty}`
        );
        const rawData = resp.data.data;
        const transformedData = transformOptions(difficulty, rawData);
        setOptions(transformedData);
      } catch (error) {
        console.error(error);
        setGameState('error');
      }
    };
    fetchOptions();
  }, [sequence, scenarioId, sequenceNumber, difficulty]);

  // 이벤트 핸들러: 시작 / 옵션 선택 / 다음 문제 / 초기화
  const handleGameStart = async () => {
    try {
      const resp = await api.post(`scenario-sessions/start`, {
        studentId,
        scenarioId,
      });
      const sessionId = resp.data.data.sessionId;
      setSessionId(sessionId);
      console.log(sessionId + 'session start');

      //   session 시작된 경우만 다음 화면으로
      setGameState('playing');
      setScreen('question');
      setIsCorrect(false);
    } catch (error) {
      console.error(error);
      setGameState('error');
    }
  };

  // 옵션 선택 화면으로 전환
  const handleSelectOption = () => {
    setScreen('option');
    setIsCorrect(false);
  };

  const handleSkip = () => {
    console.log('스킵');
    alert('스킵합니다');

    setTimeout(() => {
      // 정답인 경우
      const isLastSequence = sequenceNumber >= (scenario?.totalSequences || 0);

      if (isLastSequence) {
        setScreen('end');
      } else {
        setSequenceNumber((prev) => prev + 1);
        setScreen('question');
      }
    }, 1000);

    return;
  };

  // 피드백 처리 & 다음 시퀀스로 이동
  const handleFeedback = async (selectedOption: TransformedOption) => {
    try {
      const resp = await api.post(`scenario-sessions/submit-answer`, {
        sessionId,
        scenarioId,
        sequenceNumber,
        selectedOptionId: selectedOption.id,
      });

      const { correct } = resp.data.data;

      setIsCorrect(correct);
      setScreen('feedback');

      setTimeout(() => {
        if (!correct) {
          setScreen('option');
          return;
        }

        // 정답인 경우
        const isLastSequence =
          sequenceNumber >= (scenario?.totalSequences || 0);

        // TODO: END logic 통일
        if (isLastSequence) {
          setScreen('end');
        } else {
          setSequenceNumber((prev) => prev + 1);
          setScreen('question');
        }
      }, 2000);
    } catch (error) {
      console.error(error);
      setGameState('error');
    }
  };

  const handleRestart = () => {
    setGameState('loading');
    setScreen('start');
    setSequenceNumber(1);
    setIsCorrect(false);
  };

  const handleEnd = async () => {
    try {
      const resp = await api.post(`scenario-sessions/complete`, {
        sessionId,
      });
      if (resp.data.status !== 'SUCCESS') {
        throw new Error();
      }
      console.log(sessionId + 'session end');

      setGameState('end');
      setScreen('end');
    } catch (error) {
      console.error(error);
      setGameState('error');
    }
  };

  // 화면 렌더링 조건 분기
  const renderContent = () => {
    // 시나리오별
    switch (gameState) {
      case 'loading':
        return <div>로딩 중 ...</div>;
      case 'error':
        return <div>에러 발생</div>;
      case 'playing':
        switch (screen) {
          case 'start':
            return scenario ? (
              <StartScreen
                scenario={scenario}
                difficulty={difficulty ?? ''}
                onStart={handleGameStart}
              />
            ) : null;
          case 'question':
            return sequence ? (
              <SequenceScreen sequence={sequence} onNext={handleSelectOption} />
            ) : null;
          case 'option':
            return options && sequence ? (
              <OptionScreen
                options={options}
                sequence={sequence}
                onSelect={handleFeedback}
                onSkip={handleSkip}
                sessionId={sessionId}
                sequenceNumber={sequenceNumber}
                difficulty={difficulty ?? ''}
              />
            ) : null;
          case 'feedback':
            return <FeedbackScreen isCorrect={isCorrect} />;
          case 'end':
            return <EndScreen onRestart={handleRestart} onExit={handleEnd} />;
        }
    }
  };
  //   실 렌더링
  //   TODO: 캐릭터 기본 이미지 생성
  //   TODO: 백그라운드 기본 이미지 생성
  // 현재 s3 업로드된 이미지 받아올 수는 있으나 카테고리 미구현 상태
  return (
    <ScenarioLayout
      defaultBackgroundImg={bgCinema}
      backgroundUrl={scenario?.backgroundUrl ?? bgCinema}
      characterImg={
        // scenario?.characterImage ||
        girlNormal
      }
      showCharacter={screen !== 'start' && screen !== 'option'}
      blurBackground={screen === 'start' || screen === 'option'}
    >
      {renderContent()}
    </ScenarioLayout>
  );
}
