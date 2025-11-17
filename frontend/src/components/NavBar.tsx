/** @format */

import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../styles/NavBar.css';
import {
  HiOutlineHome,
  HiOutlineClock,
} from 'react-icons/hi2';

import { useUserStore } from '../stores/useUserStore';
import { useAIGenerationStore } from '../stores/useSenarioAICreateStore';
import { HiOutlineMenu, HiOutlineX } from 'react-icons/hi';

export default function NavBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { studentId, studentName } = useUserStore();
  const { isGenerating, generatedScenario, clearGeneration } = useAIGenerationStore();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [teacherName, setTeacherName] = useState('선생님');

  // localStorage에서 사용자 정보 가져오기
  useEffect(() => {
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);
        if (userInfo.name) {
          setTeacherName(userInfo.name);
        }
      } catch (error) {
        console.error('userInfo 파싱 실패:', error);
      }
    }
  }, []);

  const handleStudentClick = () => {
    if (!studentId) return;
    navigate(`/students/${studentId}`);
  };

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link to="/main">
          <img src="/logo_text.png" alt="로고" className="nav-logo-img" />
        </Link>

        <div className="navbar-desktop-links">
          <Link
            to="/main"
            className={`nav-item ${
              location.pathname === '/main' ? 'active' : ''
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
              location.pathname.startsWith('/scenarios') ? 'active' : ''
            }`}
          >
            <i className="nav-icon">
              <HiOutlineClock />
            </i>
            <span>시나리오 실행하기</span>
          </Link>
        </div>
      </div>

      <div className="navbar-right">
        {/* AI 생성 상태 인디케이터 */}
        {isGenerating && (
          <div className="ai-generation-indicator">
            <span className="spinner-small"></span>
            <span className="generation-text">AI 시나리오 생성 중...</span>
          </div>
        )}

        {/* 생성 완료 알림 (클릭 시 사라짐) */}
        {!isGenerating && generatedScenario && (
          <div className="ai-generation-complete" onClick={clearGeneration}>
            <span className="complete-icon">✓</span>
            <span className="complete-text">시나리오 생성 완료!</span>
          </div>
        )}

        <div className="selected-student" onClick={handleStudentClick}>
          {studentId && studentName ? (
            <span className="selected-student-name">
              <strong>{studentName}</strong> 학생 관리 상태입니다
            </span>
          ) : (
            <span className="selected-student-empty">
              현재 선택된 학생 없음
            </span>
          )}
        </div>

        <span className="navbar-greeting">
          <strong>{teacherName}</strong>님, 안녕하세요!
        </span>
        {/* <i className="profile-icon">
          <HiOutlineUserCircle />
        </i> */}
      </div>

      {/* Hamburger Menu Button */}
      <div className="navbar-hamburger" onClick={toggleMenu}>
        {isMenuOpen ? <HiOutlineX /> : <HiOutlineMenu />}
      </div>

      {/* Mobile Menu */}
      {isMenuOpen && (
        <div className="navbar-mobile-links">
          <Link to="/main" className="nav-item" onClick={toggleMenu}>
            메인 페이지
          </Link>
          <Link to="/scenarios" className="nav-item" onClick={toggleMenu}>
            시나리오 실행하기
          </Link>
          <div className="mobile-user-info">
            <span className="navbar-greeting">
              <strong>{teacherName}</strong>님, 안녕하세요!
            </span>
            <div
              className="selected-student"
              onClick={() => {
                handleStudentClick();
                toggleMenu();
              }}
            >
              {studentId && studentName ? (
                <span className="selected-student-name">
                  <strong>{studentName}</strong> 학생
                </span>
              ) : (
                <span className="selected-student-empty">선택된 학생 없음</span>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  );
}
