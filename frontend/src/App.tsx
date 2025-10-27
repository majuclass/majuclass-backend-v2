// 개발자 페이지
/** @format */

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import StartPage from "./pages/(StartPage)/startpage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<StartPage />} />
        <Route path="/startpage" element={<Navigate to="/" replace />} />
        {/* 다른 라우트들... */}
      </Routes>
    </BrowserRouter>
  );
}
