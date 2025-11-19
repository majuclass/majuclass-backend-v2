/** @format */

import { useNavigate } from "react-router-dom";

interface ScenarioLayoutProps {
  defaultBackgroundImg: string;
  backgroundUrl: string;
  characterImg: string;
  showCharacter?: boolean;
  blurBackground?: boolean;
  children: React.ReactNode;
}

/** 시나리오 진행 모든 스크린이 사용할 screen의 wrapper
 * @prop {string} defaultBackgroundImg - 카테고리별 제공되는 기본 화면
 * @prop {string} backgroundUrl - 배경 이미지 URL
 * @prop {string} characterImg - 캐릭터 이미지 URL
 * @prop {boolean} [showCharacter=true] - 캐릭터 full body 보이기
 * @prop {boolean} [blurBackground=false] - 배경 블러 처리
 * @prop {React.ReactNode} children - 시나리오 내부 화면 요소
 */
export default function ScenarioLayout({
  defaultBackgroundImg,
  backgroundUrl,
  characterImg,
  showCharacter = true,
  blurBackground = false,
  children,
}: ScenarioLayoutProps) {
  //   시나리오 화면 탈출하는 함수
  const navigate = useNavigate();

  // 배경 이미지 에러처리 함수
  const handleBackgroundError = (
    e: React.SyntheticEvent<HTMLImageElement, Event>
  ) => {
    // S3 URL 로드 실패 시, Prop으로 받은 기본 이미지 URL로 대체
    e.currentTarget.src = defaultBackgroundImg;

    // 무한 루프 방지를 위해 onError 핸들러 제거
    e.currentTarget.onerror = null;

    // 대체 후 이미지 핏 조정
    e.currentTarget.style.objectFit = "cover";
  };

  return (
    <div className="relative w-full h-screen font-shark">
      {/* 배경 이미지 img 태그로 변경 & onError 적용 */}
      <img
        src={backgroundUrl}
        alt="배경이미지"
        onError={handleBackgroundError}
        className="absolute inset-0 w-full h-full object-cover z-0"
      />

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
        <div
          onClick={() => navigate(-1)}
          className="absolute top-4 left-10 z-50 text-2xl cursor-pointer select-none"
        >
          {"< 뒤로가기"}
        </div>
        {children}
      </div>
    </div>
  );
}
