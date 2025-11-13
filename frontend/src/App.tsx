/** @format */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import StartPage from './pages/StartPage';
import MainPage from './pages/MainPage';
import ScenarioListPage from './pages/ScenarioListPage';
import SimulationPage from './pages/SimulationPage';
import DashBoardPage from './pages/DashBoardPage';
import SelectLevelPage from './pages/SelectLevelPage';
import ScenarioCreatePage from './pages/ScenarioCreatePage';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  // tanstack query
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        // 실패 시 재시도 3회
        retry: 3,
      },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <BrowserRouter>
          <Routes>
            {/*  */}
            {/* 시작 페이지 */}
            <Route path="/" element={<StartPage />} />
            <Route path="/startpage" element={<Navigate to="/" replace />} />
            {/* 메인 페이지 */}
            <Route path="/main" element={<MainPage />} />
            {/* 회원가입 페이지 */}
            <Route
              path="/signup"
              element={<div>회원가입 준비 중입니다.</div>}
            />
            {/* 시나리오 목록 페이지 */}
            <Route path="/scenarios" element={<ScenarioListPage />} />
            {/* 시나리오 생성 */}
            <Route path="/scenarios/create" element={<ScenarioCreatePage />} />
            {/* 시나리오 레벨 선택 페이지 */}
            <Route
              path="/simulation/:scenarioId"
              element={<SelectLevelPage />}
            />
            {/* 시나리오 실행 페이지 */}
            <Route
              path="/simulation/:scenarioId/:difficulty"
              element={<SimulationPage />}
            />
            {/* 학생 대시보드 페이지 */}
            <Route path="/students/:id" element={<DashBoardPage />} />
            {/* 404 fallback */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </BrowserRouter>
      </ErrorBoundary>
    </QueryClientProvider>
  );
}
