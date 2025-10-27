import Lottie from "lottie-react"
import "./startpage.css"
import LoginCard from "./components/logincard"
import characterImg1 from "./assets/hello.png"
// import characterImg2 from "./assets/hello.png"
import bgAnimation from "./assets/Animated background - no balloon.json";



export default function StartPage() {
  return (
    <main className="startpage-bg">
        <Lottie
            animationData={bgAnimation}
            loop    
            autoplay
            className="bg-lottie"
            rendererSettings={{ preserveAspectRatio: "xMidYMid slice"}}
        />

        <section className="scene">
            <img
                src={characterImg1}
                alt="기본 캐릭터"
                className = "character left"
                draggable={false}
            />    
        </section>

        <div className="logincard">
            <LoginCard/>
        </div>
    </main>
  );
}
