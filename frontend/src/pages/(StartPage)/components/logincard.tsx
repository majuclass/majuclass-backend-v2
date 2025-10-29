// src/pages/(StartPage)/components/logincard.tsx
import React from "react";
import { useLogin } from "../hooks/userlogin";

export default function LoginCard() {
  const {
    username,
    setUsername,
    password,
    setPassword,
    loading,
    error,
    submit,
  } = useLogin();

  return (
    <div className="w-full max-w-[420px] rounded-2xl bg-white/80 shadow-xl backdrop-blur p-6">
      <h2 className="text-2xl font-bold mb-4 text-center">로그인</h2>

      <form onSubmit={submit} className="space-y-4">
        {/* 아이디 */}
        <div>
          <label
            className="block text-sm font-medium mb-1 text-gray-700"
            htmlFor="username"
          >
            아이디
          </label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400"
            placeholder="아이디를 입력하세요"
            autoComplete="username"
            disabled={loading}
          />
        </div>

        {/* 비밀번호 */}
        <div>
          <label
            className="block text-sm font-medium mb-1 text-gray-700"
            htmlFor="password"
          >
            비밀번호
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400"
            placeholder="비밀번호를 입력하세요"
            autoComplete="current-password"
            disabled={loading}
          />
        </div>

        {/* 에러 메시지 */}
        {error && (
          <p className="text-sm text-red-600 text-center" role="alert">
            {error}
          </p>
        )}

        {/* 로그인 버튼 */}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg px-4 py-2 font-semibold shadow disabled:opacity-50 bg-blue-500 text-white hover:bg-blue-600 transition"
        >
          {loading ? "로그인 중..." : "로그인"}
        </button>
      </form>
    </div>
  );
}
