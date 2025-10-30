/** @format */

import { useNavigate } from "react-router-dom";

interface ScenarioLayoutProps {
  backgroundImg: string;
  characterImg: string;
  showCharacter?: boolean;
  blurBackground?: boolean;
  children: React.ReactNode;
}

/** 시나리오 진행 모든 스크린이 사용할 screen의 wrapper
 * @param backgroundImg - 배경 이미지 URL
 * @param characterImg - 캐릭터 이미지 URL
 * @param showCharacter - 캐릭터 full body 보이기 (기본 true)
 * @param blurBackground - 배경 블러 처리 (기본 false)
 * @param children - 시나리오 내부 화면 요소
 */
export default function ScenarioLayout({
  backgroundImg,
  characterImg,
  showCharacter = true,
  blurBackground = false,
  children,
}: ScenarioLayoutProps) {
  //   시나리오 화면 탈출하는 함수
  const navigate = useNavigate();

  return (
    <div
      className="relative w-full h-screen bg-cover bg-center font-shark"
      style={{ backgroundImage: `url(${backgroundImg})` }}
    >
      {blurBackground && (
        <div className="absolute inset-0 backdrop-blur-md bg-white/10 z-0" />
      )}
      {/* 직원 / 캐릭터 */}
      {showCharacter && characterImg && (
        <img
          src={characterImg}
          alt="character"
          className="absolute top-1/2 left-1/2 w-[480px] transform -translate-x-1/2 -translate-y-[55%]"
        />
      )}

      {/* 시나리오 화면 (Start / Sequence / Feedback 등) */}
      <div className="relative z-10 w-full h-full flex items-center justify-center">
        {/* 시나리오 중단하고 나가기 */}
        <div
          onClick={() => navigate(-1)}
          className="absolute top-4 left-10 z-50 text-2xl cursor-pointer select-none"
        >
          {"< 나가기"}
        </div>
        {children}
      </div>
    </div>
  );
}
