import { useState, useEffect } from "react"
import Lottie from "lottie-react"
import "./startpage.css"
import LoginCard from "./components/logincard"
import NormalCharacter from "./assets/normal.png"
import HelloCharacter from "./assets/hello.png"
import BackgroundAnimation from "./assets/Animated background - no balloon.json";

export default function StartPage() {
    const [showHello, setShowHello] = useState(false)
    const [showBubble, setShowBubble] = useState(false)

    useEffect(() => {
        const helloTimer = setTimeout(() => {
            setShowHello(true)
            setTimeout(() =>{
                setShowBubble(true)
            }, 300)
        }, 1000)

        return () => clearTimeout(helloTimer)
    }, []) // 렌더 1회 실행만
    return (
        <main className="startpage-bg">
            <Lottie
                animationData={BackgroundAnimation}
                loop
                autoPlay
                className="bg-lottie"
                rendererSettings={{
                    preserveAspectRatio: "xMidYMid slice"
                }}
            />
            <section className="content-wrapper">
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
                                    <span className="greeting-text">하이</span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
                <div className="login-section">
                    <LoginCard />
                </div>
            </section>
        </main>
    );
}