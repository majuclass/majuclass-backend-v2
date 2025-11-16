// src/pages/StartPage.tsx
import { useState, useEffect, useMemo } from 'react';
import Lottie from 'lottie-react';
import '../styles/StartPage.css';
import LoginCard from '../components/startpage/LoginCard';
import NormalCharacter from '../assets/startpage/Normal.png';
import HelloCharacter from '../assets/startpage/Hello.png';
import BackgroundAnimation from '../assets/startpage/Animated background - no balloon.json';

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
        {/* 소개글 영역 */}
        <div className="introduction-section">
          <h1 className="intro-title">마주교실</h1>
          <p className="intro-subtitle">
            “선생님과 아이들이 마주보고 배워나가는 곳, 마주교실”
          </p>
          <p className="intro-description">
            발달장애·통합학급 학생이 일상 사회 상황을 안전하게 연습하고 부담
            없이 사회성 수업을 운영할 수 있는 시뮬레이션 기반 웹 교실입니다.
          </p>
        </div>

        {/* 캐릭터 영역 */}
        <div className="character-section">
          <div className="character-container">
            <img
              src={showHello ? HelloCharacter : NormalCharacter}
              alt="캐릭터"
              className={`character ${showHello ? 'hello' : 'normal'}`}
              draggable={false}
            />

            {showBubble && (
              <div className="speech-bubble">
                <div className="bubble-content">
                  <span className="greeting-text">
                    마주교실에 오신 것을 환영합니다!
                  </span>
                </div>
                <div className="greeting-subtitle">
                  학생들의 성장을 함께하는 공간입니다
                </div>
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
