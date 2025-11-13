/** NavBar, Footer 등 모든 페이지 유지하는 레이아웃 컴포넌트 */
import { Outlet, useLocation } from 'react-router-dom';
import NavBar from '../NavBar';

interface LayoutProps {
  children?: React.ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const location = useLocation();
  const content = children ? children : <Outlet />;

  const hideNavPaths = [/^\/simulation\/.+/, /^\/$/, /^\/login$/, /^\/signup$/];

  const shouldHideNav = hideNavPaths.some((pattern) =>
    pattern.test(location.pathname)
  );

  return (
    <div className="app-container">
      {!shouldHideNav && <NavBar />}

      <main>{content}</main>

      {/* TODO */}
      {/* <Footer /> */}
    </div>
  );
}
