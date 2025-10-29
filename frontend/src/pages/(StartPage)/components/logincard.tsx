// components/LoginCard.tsx

import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../hooks/userlogin";

export default function LoginCard() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  
  // Custom Hook 사용
  const { login, isLoading } = useAuth();

  // 아이디 저장 기능 - 컴포넌트 마운트 시 체크
  useEffect(() => {
    const savedRemember = localStorage.getItem("remember_username") === "true";
    const savedUsername = localStorage.getItem("saved_username") || "";
    
    setRememberMe(savedRemember);
    if (savedRemember && savedUsername) {
      setUsername(savedUsername);
    }
  }, []);

  // 아이디 저장 체크박스 변경 처리
  useEffect(() => {
    if (rememberMe) {
      localStorage.setItem("remember_username", "true");
      if (username) {
        localStorage.setItem("saved_username", username);
      }
    } else {
      localStorage.setItem("remember_username", "false");
      localStorage.removeItem("saved_username");
    }
  }, [rememberMe, username]);

  // 폼 제출 가능 여부
  const canSubmit = username.trim().length > 0 && password.trim().length > 0 && !isLoading;

  // 로그인 폼 제출 처리
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;

    try {
      setLocalError(null);
      // useAuth의 login 함수 호출
      await login({
        username: username.trim(),
        password: password,
        rememberMe
      });
    } catch (error) {
      // 에러 메시지 표시
      const errorMessage = error instanceof Error ? error.message : '로그인에 실패했습니다.';
      setLocalError(errorMessage);
    }
  };

  return (
    <div className="login-card">
      <h1 className="login-title">Login</h1>

      <form onSubmit={handleSubmit} className="login-form" noValidate>
        {/* 아이디 입력 필드 */}
        <div className="form-field">
          <label htmlFor="username" className="sr-only">
            아이디
          </label>
          <input
            id="username"
            name="username"
            type="text"
            placeholder="아이디"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            disabled={isLoading}
            className={localError ? "error" : ""}
          />
        </div>

        {/* 비밀번호 입력 필드 */}
        <div className="form-field password-field">
          <label htmlFor="password" className="sr-only">
            비밀번호
          </label>
          <input
            id="password"
            name="password"
            type={showPassword ? "text" : "password"}
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            disabled={isLoading}
            className={localError ? "error" : ""}
          />
          <button
            type="button"
            className="password-toggle"
            onClick={() => setShowPassword(!showPassword)}
            aria-label={showPassword ? "비밀번호 숨기기" : "비밀번호 표시"}
            tabIndex={-1}
          >
            {showPassword ? "👁️" : "👁️‍🗨️"}
          </button>
        </div>

        {/* 아이디 저장 체크박스 */}
        <div className="form-options">
          <label className="remember-checkbox">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              disabled={isLoading}
            />
            <span>아이디 저장</span>
          </label>
        </div>

        {/* 에러 메시지 표시 */}
        {localError && (
          <div className="error-message" role="alert">
            {localError}
          </div>
        )}

        {/* 로그인 버튼 */}
        <button 
          type="submit" 
          className="login-button"
          disabled={!canSubmit}
        >
          {isLoading ? "로그인 중..." : "로그인"}
        </button>

        {/* 추가 링크들 */}
        <div className="login-links">
          <span className="signup-text">
            아직 회원이 아니신가요?{" "}
            <Link to="/signup" className="signup-link">
              회원가입
            </Link>
          </span>
        </div>

        {/* 또는 구분선 (선택사항) */}
        <div className="divider">
          <span>또는</span>
        </div>

        {/* 소셜 로그인 버튼들 (선택사항) */}
        <div className="social-login">
          <button type="button" className="social-button" disabled={isLoading}>
            <span>🔑</span> 간편 로그인
          </button>
        </div>
      </form>
    </div>
  );
}