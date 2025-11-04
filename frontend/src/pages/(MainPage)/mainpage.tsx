// src/pages/(MainPage)/mainpage.tsx
import NavBar from "./components/navbar";
import Record from "../../components/scenario/audio/AudioRecordingButton";

export default function MainPage() {
  return (
    <>
    <div className="min-h-screen bg-gray-50">
      <NavBar />
    </div> 
    <div>
    <Record />
    </div>
    </>
  );
}
