/* eslint-disable @typescript-eslint/no-explicit-any */

// src/pages/(StartPage)/components/logincard.tsx
import React, { useMemo, useState } from "react";
import { useLogin } from "../../hooks/startpage/UserLogin";

export default function LoginCard() {
  const {
    username,
    setUsername,
    password,
    setPassword,
    loading,
    error,      // 서버에서 온 에러 메시지 (예: "아이디 또는 비밀번호가 올바르지 않습니다.")
    submit,
  } = useLogin();

  // 클라이언트 즉시 검증 (백엔드 규칙과 맞추세요)
  const [touched, setTouched] = useState({ username: false, password: false });
  const [showPw, setShowPw] = useState(false);
  const [capsOn, setCapsOn] = useState(false);

  const localErrors = useMemo(() => {
    const errs: { username?: string; password?: string } = {};
    const u = username.trim();
    if (!u) errs.username = "아이디를 입력하세요.";
    else if (u.length < 3) errs.username = "아이디는 3자 이상 입력하세요.";
    else if (u.length > 20) errs.username = "아이디는 20자 이내여야 합니다.";

    if (!password) errs.password = "비밀번호를 입력하세요.";
    else if (password.length < 8) errs.password = "비밀번호는 8자 이상 입력하세요.";
    else if (password.length > 64) errs.password = "비밀번호는 64자 이내여야 합니다.";

    return errs;
  }, [username, password]);

  const hasClientError = Object.keys(localErrors).length > 0;

  const onSubmit: React.FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    // 로딩 중/클라이언트 오류 시 서버 요청 차단
    if (loading || hasClientError) return;
    submit(e);
  };

  return (
    <div className="w-full max-w-[420px] rounded-2xl bg-white/80 shadow-xl backdrop-blur p-6">
      <h2 className="text-2xl font-bold mb-4 text-center">로그인</h2>

      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {/* 아이디 */}
        <div>
          <label
            className={`block text-sm font-medium mb-1 ${
              touched.username && localErrors.username ? "text-red-700" : "text-gray-700"
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
              ${touched.username && localErrors.username ? "border-red-500 focus:ring-red-400" : "border-gray-300 focus:ring-blue-400"}
            `}
            placeholder="아이디를 입력하세요"
            autoComplete="username"
            inputMode="text"
            autoCapitalize="none"
            spellCheck={false}
            disabled={loading}
            aria-invalid={!!(touched.username && localErrors.username)}
            aria-describedby={touched.username && localErrors.username ? "username-error" : undefined}
          />
          {touched.username && localErrors.username && (
            <p id="username-error" className="mt-1 text-xs text-red-600">
              {localErrors.username}
            </p>
          )}
        </div>

        {/* 비밀번호 */}
        <div>
          <div className="flex items-center justify-between">
            <label
              className={`block text-sm font-medium mb-1 ${
                touched.password && localErrors.password ? "text-red-700" : "text-gray-700"
              }`}
              htmlFor="password"
            >
              비밀번호
            </label>
            {capsOn && (
              <span className="text-xs text-amber-700 bg-amber-100 px-2 py-0.5 rounded">
                CapsLock이 켜져 있습니다
              </span>
            )}
          </div>

          <div className="relative">
            <input
              id="password"
              name="password"
              type={showPw ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, password: true }))}
              onKeyUp={(e) => {
                const ev = e as unknown as KeyboardEvent;
                // 일부 브라우저는 getModifierState만 지원
                setCapsOn((ev as any).getModifierState?.("CapsLock") ?? false);
              }}
              className={`w-full rounded-lg border px-3 py-2 pr-10 outline-none focus:ring-2
                ${touched.password && localErrors.password ? "border-red-500 focus:ring-red-400" : "border-gray-300 focus:ring-blue-400"}
              `}
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
              autoCapitalize="none"
              spellCheck={false}
              disabled={loading}
              aria-invalid={!!(touched.password && localErrors.password)}
              aria-describedby={touched.password && localErrors.password ? "password-error" : undefined}
            />

            <button
              type="button"
              className="absolute inset-y-0 right-0 px-3 text-sm text-gray-600 hover:text-gray-800"
              onClick={() => setShowPw((v) => !v)}
              aria-label={showPw ? "비밀번호 숨기기" : "비밀번호 보기"}
              tabIndex={-1}
            >
              {showPw ? "숨김" : "보기"}
            </button>
          </div>

          {touched.password && localErrors.password && (
            <p id="password-error" className="mt-1 text-xs text-red-600">
              {localErrors.password}
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

        {/* 로그인 버튼 */}
        <button
          type="submit"
          disabled={loading || hasClientError}
          className={`w-full rounded-lg px-4 py-2 font-semibold shadow transition text-white
            ${loading || hasClientError ? "bg-blue-400 cursor-not-allowed" : "bg-blue-500 hover:bg-blue-600"}
          `}
        >
          {loading ? "로그인 중..." : "로그인"}
        </button>
      </form>
    </div>
  );
}
