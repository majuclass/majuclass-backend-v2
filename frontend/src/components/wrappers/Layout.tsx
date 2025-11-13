/** NavBar, Footer 등 모든 페이지 유지하는 레이아웃 컴포넌트 */
import { Outlet } from 'react-router-dom';
import NavBar from '../NavBar';

interface LayoutProps {
  children?: React.ReactNode;
}

// TODO: layout에서 시나리오 실행 전체 제외
export default function Layout({ children }: LayoutProps) {
  const content = children ? children : <Outlet />;

  return (
    <div className="app-container">
      <NavBar />

      <main>{content}</main>

      {/* TODO */}
      {/* <Footer /> */}
    </div>
  );
}
