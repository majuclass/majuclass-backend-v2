// 개발자 페이지
/** @format */

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import StartPage from "./pages/StartPage";
import MainPage from "./pages/MainPage";
import ScenarioListPage from "./pages/ScenarioListPage";
import SimulationPage from "./pages/SimulationPage";
import StudentsPage from "./pages/StudentsPage"
import DashBoardPage from "./pages/DashBoardPage"

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/*  */}
        {/* 시작 페이지 */}
        <Route path="/" element={<StartPage />} />
        <Route path="/startpage" element={<Navigate to="/" replace />} />
        {/* 메인 페이지 */}
        <Route path="/main" element={<MainPage />}/>
        {/* 회원가입 페이지 */}
        <Route path="/signup" element={<div>회원가입 준비 중입니다.</div>} />
        {/* 시나리오 목록 페이지 */}
        <Route path="/scenarios" element={<ScenarioListPage />}/>
        {/* 시나리오 페이지 */}
        <Route path="/simulation/:scenarioId" element={<SimulationPage />}/>
        {/* 학생 목록 페이지 */}
        <Route path="/students" element={<StudentsPage />}/>
        {/* 학생 대시보드 페이지 */}
        <Route path="/students/:id" element={<DashBoardPage />}/>
        {/* 다른 라우터는 아래 추가 */}
      </Routes>
    </BrowserRouter>
  )
}
