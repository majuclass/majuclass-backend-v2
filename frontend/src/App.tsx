// 개발자 페이지
/** @format */

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import StartPage from "./pages/(StartPage)/startpage";
import MainPage from "./pages/(MainPage)/mainpage";
import ScenarioListPage from "./pages/(ScenarioListPage)/scenariolistpage";
import SimulationPage from "./pages/SimulationPage";

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
        {/* 다른 라우터는 아래 추가 */}
      </Routes>
    </BrowserRouter>
  )
}
