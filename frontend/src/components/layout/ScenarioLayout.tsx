/** @format
 * 모든 screen이 사용할 scenarioWrapper
 */

interface ScenarioLayoutProps {
  backgroundImg: string;
  characterImg: string;
  showCharacter?: boolean;
  blurBackground?: boolean;
  children: React.ReactNode;
}

export default function ScenarioLayout({
  backgroundImg,
  characterImg,
  showCharacter = true,
  blurBackground = false,
  children,
}: ScenarioLayoutProps) {
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
        {children}
      </div>
    </div>
  );
}
