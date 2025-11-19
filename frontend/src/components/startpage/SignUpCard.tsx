/** @format */

import React, { useMemo, useState } from 'react';
import { useSignUp } from '../../hooks/startpage/useSignUp';

interface SignUpCardProps {
  onSwitchToLogin: () => void;
}

export default function SignUpCard({ onSwitchToLogin }: SignUpCardProps) {
  const {
    username,
    setUsername,
    email,
    setEmail,
    name,
    setName,
    password,
    setPassword,
    passwordConfirm,
    setPasswordConfirm,
    loading,
    error,
    submit,
  } = useSignUp(onSwitchToLogin);

  const [touched, setTouched] = useState({
    username: false,
    email: false,
    name: false,
    password: false,
    passwordConfirm: false,
  });
  const [showPw, setShowPw] = useState(false);
  const [showPwConfirm, setShowPwConfirm] = useState(false);

  // 클라이언트 검증
  const localErrors = useMemo(() => {
    const errs: {
      username?: string;
      email?: string;
      name?: string;
      password?: string;
      passwordConfirm?: string;
    } = {};

    // 아이디 검증
    const u = username.trim();
    if (!u) errs.username = '아이디를 입력하세요.';
    else if (u.length < 3) errs.username = '아이디는 3자 이상 입력하세요.';
    else if (u.length > 50) errs.username = '아이디는 50자 이내여야 합니다.';

    // 이메일 검증
    const e = email.trim();
    if (!e) errs.email = '이메일을 입력하세요.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e))
      errs.email = '올바른 이메일 형식이 아닙니다.';
    else if (e.length > 200) errs.email = '이메일은 200자 이내여야 합니다.';

    // 닉네임 검증
    const n = name.trim();
    if (!n) errs.name = '닉네임을 입력하세요.';
    else if (n.length > 50) errs.name = '닉네임은 50자 이내여야 합니다.';

    // 비밀번호 검증
    if (!password) errs.password = '비밀번호를 입력하세요.';
    else if (password.length < 8)
      errs.password = '비밀번호는 8자 이상 입력하세요.';
    else if (password.length > 64)
      errs.password = '비밀번호는 64자 이내여야 합니다.';

    // 비밀번호 확인 검증
    if (!passwordConfirm)
      errs.passwordConfirm = '비밀번호 확인을 입력하세요.';
    else if (password !== passwordConfirm)
      errs.passwordConfirm = '비밀번호가 일치하지 않습니다.';

    return errs;
  }, [username, email, name, password, passwordConfirm]);

  const hasClientError = Object.keys(localErrors).length > 0;

  const onSubmit: React.FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    if (loading || hasClientError) return;
    submit(e);
  };

  return (
    <div className="w-full max-w-[480px] rounded-2xl bg-white/80 shadow-xl backdrop-blur p-6">
      <h2 className="text-2xl font-bold mb-4 text-center">회원가입</h2>

      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {/* 아이디 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.username && localErrors.username
                ? 'text-red-700'
                : 'text-gray-700'
            }`}
            htmlFor="username"
          >
            아이디
          </label>
          <input
            id="username"
            name="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onBlur={() => setTouched((t) => ({ ...t, username: true }))}
            className={`w-full rounded-lg border px-3 py-2 outline-none focus:ring-2
              ${
                touched.username && localErrors.username
                  ? 'border-red-500 focus:ring-red-400'
                  : 'border-gray-300 focus:ring-blue-400'
              }
            `}
            placeholder="아이디를 입력하세요 (3~50자)"
            autoComplete="username"
            disabled={loading}
            aria-invalid={!!(touched.username && localErrors.username)}
          />
          {touched.username && localErrors.username && (
            <p className="mt-1 text-xs text-red-600">
              {localErrors.username}
            </p>
          )}
        </div>

        {/* 이메일 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.email && localErrors.email
                ? 'text-red-700'
                : 'text-gray-700'
            }`}
            htmlFor="email"
          >
            이메일
          </label>
          <input
            id="email"
            name="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onBlur={() => setTouched((t) => ({ ...t, email: true }))}
            className={`w-full rounded-lg border px-3 py-2 outline-none focus:ring-2
              ${
                touched.email && localErrors.email
                  ? 'border-red-500 focus:ring-red-400'
                  : 'border-gray-300 focus:ring-blue-400'
              }
            `}
            placeholder="example@email.com"
            autoComplete="email"
            disabled={loading}
            aria-invalid={!!(touched.email && localErrors.email)}
          />
          {touched.email && localErrors.email && (
            <p className="mt-1 text-xs text-red-600">{localErrors.email}</p>
          )}
        </div>

        {/* 닉네임 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.name && localErrors.name
                ? 'text-red-700'
                : 'text-gray-700'
            }`}
            htmlFor="name"
          >
            닉네임
          </label>
          <input
            id="name"
            name="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            onBlur={() => setTouched((t) => ({ ...t, name: true }))}
            className={`w-full rounded-lg border px-3 py-2 outline-none focus:ring-2
              ${
                touched.name && localErrors.name
                  ? 'border-red-500 focus:ring-red-400'
                  : 'border-gray-300 focus:ring-blue-400'
              }
            `}
            placeholder="닉네임을 입력하세요"
            autoComplete="name"
            disabled={loading}
            aria-invalid={!!(touched.name && localErrors.name)}
          />
          {touched.name && localErrors.name && (
            <p className="mt-1 text-xs text-red-600">{localErrors.name}</p>
          )}
        </div>

        {/* 비밀번호 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.password && localErrors.password
                ? 'text-red-700'
                : 'text-gray-700'
            }`}
            htmlFor="password"
          >
            비밀번호
          </label>
          <div className="relative">
            <input
              id="password"
              name="password"
              type={showPw ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, password: true }))}
              className={`w-full rounded-lg border px-3 py-2 pr-10 outline-none focus:ring-2
                ${
                  touched.password && localErrors.password
                    ? 'border-red-500 focus:ring-red-400'
                    : 'border-gray-300 focus:ring-blue-400'
                }
              `}
              placeholder="비밀번호를 입력하세요 (8~64자)"
              autoComplete="new-password"
              disabled={loading}
              aria-invalid={!!(touched.password && localErrors.password)}
            />
            <button
              type="button"
              className="absolute inset-y-0 right-0 px-3 text-sm text-gray-600 hover:text-gray-800"
              onClick={() => setShowPw((v) => !v)}
              aria-label={showPw ? '비밀번호 숨기기' : '비밀번호 보기'}
              tabIndex={-1}
            >
              {showPw ? '숨김' : '보기'}
            </button>
          </div>
          {touched.password && localErrors.password && (
            <p className="mt-1 text-xs text-red-600">{localErrors.password}</p>
          )}
        </div>

        {/* 비밀번호 확인 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.passwordConfirm && localErrors.passwordConfirm
                ? 'text-red-700'
                : 'text-gray-700'
            }`}
            htmlFor="passwordConfirm"
          >
            비밀번호 확인
          </label>
          <div className="relative">
            <input
              id="passwordConfirm"
              name="passwordConfirm"
              type={showPwConfirm ? 'text' : 'password'}
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              onBlur={() =>
                setTouched((t) => ({ ...t, passwordConfirm: true }))
              }
              className={`w-full rounded-lg border px-3 py-2 pr-10 outline-none focus:ring-2
                ${
                  touched.passwordConfirm && localErrors.passwordConfirm
                    ? 'border-red-500 focus:ring-red-400'
                    : 'border-gray-300 focus:ring-blue-400'
                }
              `}
              placeholder="비밀번호를 다시 입력하세요"
              autoComplete="new-password"
              disabled={loading}
              aria-invalid={
                !!(touched.passwordConfirm && localErrors.passwordConfirm)
              }
            />
            <button
              type="button"
              className="absolute inset-y-0 right-0 px-3 text-sm text-gray-600 hover:text-gray-800"
              onClick={() => setShowPwConfirm((v) => !v)}
              aria-label={
                showPwConfirm ? '비밀번호 숨기기' : '비밀번호 보기'
              }
              tabIndex={-1}
            >
              {showPwConfirm ? '숨김' : '보기'}
            </button>
          </div>
          {touched.passwordConfirm && localErrors.passwordConfirm && (
            <p className="mt-1 text-xs text-red-600">
              {localErrors.passwordConfirm}
            </p>
          )}
        </div>

        {/* 서버 에러 메시지 */}
        <div aria-live="polite">
          {error && !loading && (
            <p className="text-sm text-red-600 text-center" role="alert">
              {error}
            </p>
          )}
        </div>

        {/* 회원가입 버튼 */}
        <button
          type="submit"
          disabled={loading || hasClientError}
          className={`w-full rounded-lg px-4 py-2 font-semibold shadow transition text-white
            ${
              loading || hasClientError
                ? 'bg-blue-400 cursor-not-allowed'
                : 'bg-blue-500 hover:bg-blue-600'
            }
          `}
        >
          {loading ? '회원가입 중...' : '회원가입'}
        </button>

        {/* 로그인 링크 */}
        <div className="text-center text-sm text-gray-600 mt-4">
          이미 계정이 있으신가요?{' '}
          <button
            type="button"
            onClick={onSwitchToLogin}
            className="text-blue-500 hover:text-blue-600 font-semibold"
          >
            로그인
          </button>
        </div>
      </form>
    </div>
  );
}
