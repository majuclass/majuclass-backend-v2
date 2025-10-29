/* eslint-disable @typescript-eslint/no-explicit-any */
// src/pages/(StartPage)/hooks/userlogin.ts
import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { login as loginApi } from "../api";

type UserInfo = {
  userId: number;
  username: string;
  name: string;
  role: string;
};

type UseLoginReturn = {
  username: string;
  setUsername: (v: string) => void;
  password: string;
  setPassword: (v: string) => void;
  loading: boolean;
  error: string | null;
  submit: (e?: React.FormEvent) => Promise<void>;
  logout: () => void;
  user: UserInfo | null;
};

export const useLogin = (): UseLoginReturn => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // 로그인 처리
  const submit = useCallback(
    async (e?: React.FormEvent) => {
      if (e) e.preventDefault();
      if (!username.trim() || !password.trim()) {
        setError("아이디와 비밀번호를 모두 입력해주세요.");
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const res = await loginApi({ username, password });

        // 토큰 및 유저정보 저장
        localStorage.setItem("accessToken", res.accessToken);
        localStorage.setItem("refreshToken", res.refreshToken);

        const userInfo: UserInfo = {
          userId: res.userId,
          username: res.username,
          name: res.name,
          role: res.role,
        };
        localStorage.setItem("userInfo", JSON.stringify(userInfo));

        // 메인 페이지로 이동
        navigate("/main");
      } catch (err: unknown) {
          let msg = "로그인에 실패했습니다.";

          if (typeof err === "object" && err !== null) {
            // AxiosError 타입일 때
            if ("response" in err && typeof (err as any).response === "object") {
              msg =
                (err as any).response?.data?.message ??
                (err as any).message ??
                "로그인에 실패했습니다.";
            } else if ("message" in err && typeof (err as any).message === "string") {
              msg = (err as any).message;
            }
          }

          setError(msg);
        } finally {
        setLoading(false);
      }
    },
    [username, password, navigate]
  );

  // 로그아웃 처리
  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userInfo");
    navigate("/");
  }, [navigate]);

  // 저장된 유저정보 반환
  const user = (() => {
    const s = localStorage.getItem("userInfo");
    if (!s) return null;
    try {
      return JSON.parse(s) as UserInfo;
    } catch {
      return null;
    }
  })();

  return {
    username,
    setUsername,
    password,
    setPassword,
    loading,
    error,
    submit,
    logout,
    user,
  };
};
