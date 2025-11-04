/** @format */

// src/pages/(MainPage)/components/navbar.tsx
import { Link, useLocation } from "react-router-dom";
import "../styles/NavBar.css";
import {
  HiOutlineHome,
  HiOutlineClock,
  HiOutlineFolder,
  HiOutlineChatBubbleBottomCenterText,
  HiOutlineUserCircle,
} from "react-icons/hi2";

export default function NavBar() {
  const location = useLocation();

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link
          to="/main"
          className={`nav-item ${
            location.pathname === "/main" ? "active" : ""
          }`}
        >
          <i className="nav-icon">
            <HiOutlineHome />
          </i>
          <span>메인 페이지</span>
        </Link>

        <Link
          to="/scenarios"
          className={`nav-item ${
            location.pathname === "/scenario" ? "active" : ""
          }`}
        >
          <i className="nav-icon">
            <HiOutlineClock />
          </i>
          <span>학생 시나리오</span>
        </Link>

        <Link
          to="/students"
          className={`nav-item ${
            location.pathname === "/students" ? "active" : ""
          }`}
        >
          <i className="nav-icon">
            <HiOutlineFolder />
          </i>
          <span>학생 관리</span>
        </Link>

        <Link
          to="/about"
          className={`nav-item ${
            location.pathname === "/about" ? "active" : ""
          }`}
        >
          <i className="nav-icon">
            <HiOutlineChatBubbleBottomCenterText />
          </i>
          <span>서비스 소개</span>
        </Link>
      </div>

      <div className="navbar-right">
        <span className="navbar-greeting">
          <strong>김선생님</strong>, 안녕하세요!
        </span>
        <i className="profile-icon">
          <HiOutlineUserCircle />
        </i>
      </div>
    </nav>
  );
}
