import { createBrowserRouter, Navigate } from 'react-router-dom';
import Layout from './components/wrappers/Layout';
import StartPage from './pages/StartPage';
import MainPage from './pages/MainPage';
import ScenarioListPage from './pages/ScenarioListPage';
import ScenarioCreatePage from './pages/ScenarioCreatePage';
import SelectLevelPage from './pages/SelectLevelPage';
import SimulationPage from './pages/SimulationPage';
import DashBoardPage from './pages/DashBoardPage';
import ErrorPage from './pages/ErrorPage';

/** 라우트 설정 */
const router = createBrowserRouter([
  {
    // 공통 레이아웃 렌더링
    path: '/',
    element: <Layout />,
    // errorElement로 빠지면 Layout 못 잡아줌
    errorElement: (
      <Layout>
        <ErrorPage />
      </Layout>
    ),
    children: [
      { path: '/', element: <StartPage /> },
      { path: '/startpage', element: <Navigate to="/" replace /> },
      { path: '/main', element: <MainPage /> },
      { path: '/signup', element: <div>회원가입 준비중입니다.</div> },
      {
        path: 'scenarios',
        element: <ScenarioListPage />,
      },
      {
        path: 'scenarios/create',
        element: <ScenarioCreatePage />,
      },
      { path: 'simulation/:scenarioId', element: <SelectLevelPage /> },
      {
        path: 'simulation/:scenarioId/:difficulty',
        element: <SimulationPage />,
      },
      { path: 'students/:id', element: <DashBoardPage /> },
      { path: '*', element: <ErrorPage /> },
    ],
  },
]);

export default router;
