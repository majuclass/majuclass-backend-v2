import { useState, useEffect, useMemo } from "react";
import Lottie from "lottie-react";
import "./startpage.css";
import LoginCard from "./components/LoginCard";
import NormalCharacter from "./assets/normal.png";
import HelloCharacter from "./assets/hello.png";
import BackgroundAnimation from "./assets/Animated background - no balloon.json";

export default function StartPage() {
  const [showHello, setShowHello] = useState(false);
  const [showBubble, setShowBubble] = useState(false);

  const animation = useMemo(() => BackgroundAnimation, []);

  useEffect(() => {
    const helloTimer = setTimeout(() => setShowHello(true), 1000);
    const bubbleTimer = setTimeout(() => setShowBubble(true), 1300);
    return () => {
      clearTimeout(helloTimer);
      clearTimeout(bubbleTimer);
    };
  }, []);

  return (
    <main className="startpage-bg">
      <Lottie animationData={animation} loop autoplay className="bg-lottie" />

      <section className="content-wrapper">
        {/* 캐릭터 영역 */}
        <div className="character-section">
          <div className="character-container">
            <img
              src={showHello ? HelloCharacter : NormalCharacter}
              alt="캐릭터"
              className={`character ${showHello ? "hello" : "normal"}`}
              draggable={false}
            />

            {showBubble && (
              <div className="speech-bubble">
                <div className="bubble-content">
                  <span className="greeting-text">마주교실에 오신 것을 환영합니다!</span>
                </div>
                <div className="greeting-subtitle">학생들의 성장을 함께하는 공간입니다</div>
              </div>
            )}
          </div>
        </div>

        {/* 로그인 영역 */}
        <aside className="login-section">
          <LoginCard />
        </aside>
      </section>
    </main>
  );
}
