// 개발자 페이지
/** @format */

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import StartPage from "./pages/(StartPage)/startpage";
import MainPage from "./pages/(MainPage)/mainpage";

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
        {/* 다른 라우터는 아래 추가 */}
      </Routes>
    </BrowserRouter>
  );
}
