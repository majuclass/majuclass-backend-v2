/** @format */
import { NavLink } from "react-router-dom";
import "./navber.css";

type NavberProps = {
  userName?: string; // 우측 인사말
};

export default function Navber({ userName = "선생님" }: NavberProps) {
  const links = [
    { to: "/", label: "메인 페이지", icon: HomeIcon },
    { to: "/scenario", label: "학생 시나리오", icon: SmileClockIcon },
    { to: "/students", label: "학생 관리", icon: FolderIcon },
    { to: "/about", label: "서비스 소개", icon: HatIcon },
  ];

  return (
    <header className="navber">
      <nav className="navber-inner">
        <ul className="navber-left">
          {links.map(({ to, label, icon: Icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  "nav-link" + (isActive ? " active" : "")
                }
              >
                <Icon className="nav-ic" />
                <span className="nav-text">{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>

        <div className="navber-right">
          <span className="greeting">
            <strong>{userName}</strong>, 안녕하세요!
          </span>
          <button className="avatar" aria-label="내 프로필">
            <UserIcon className="nav-ic" />
          </button>
        </div>
      </nav>
    </header>
  );
}

/* ====== Icons (inline SVG components) ====== */
function HomeIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M3 11.5L12 4l9 7.5" />
      <path d="M5 10.5V20h14v-9.5" />
    </svg>
  );
}
function SmileClockIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
      <path d="M8 14c1 1.3 2.6 2 4 2s3-.7 4-2" />
    </svg>
  );
}
function FolderIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M3 7h6l2 2h10v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z" />
    </svg>
  );
}
function HatIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <path d="M3 10l9-4 9 4-9 4-9-4z" />
      <path d="M21 14a8.5 5 0 0 1-18 0" />
    </svg>
  );
}
function UserIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
    </svg>
  );
}
