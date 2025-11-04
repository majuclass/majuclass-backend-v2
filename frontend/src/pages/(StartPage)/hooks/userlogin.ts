/* eslint-disable @typescript-eslint/no-explicit-any */
// src/pages/(StartPage)/hooks/userlogin.ts
import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { login as loginApi } from "../api"

type UserInfo = {
  userId: number;
  username: string;
  name: string;
  role: string;
};

type LoginResult = {
  accessToken: string;
  refreshToken: string;
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

// 좁히기 유틸
function isRecord(v: unknown): v is Record<string, unknown> {
  return typeof v === "object" && v !== null;
}
function pickErrorMessage(err: unknown): string | null {
  if (!isRecord(err)) return null;

  // // err.message
  // // 아래 if ~ mag;는 사용자 입장에서 좋지 못한 구조
  // if (typeof err.message === "string") return err.message;

  // // err.response?.data?.message (Axios 유사 구조)
  // const response = isRecord(err.response) ? (err.response as Record<string, unknown>) : null;
  // const data = response && isRecord(response.data) ? (response.data as Record<string, unknown>) : null;
  // const msg = data && typeof data.message === "string" ? data.message : null;
  // return msg;

  // 서버 응답 메시지를 먼저 확인 (Axios response.data.message)
  const response = isRecord(err.response) ? (err.response as Record<string, unknown>) : null;
  const data = response && isRecord(response.data) ? (response.data as Record<string, unknown>) : null;
  if (data && typeof data.message === "string" && data.message.trim() !== "") {
    return data.message;
  }

  // 그 다음 axios 기본 message 확인
  if (typeof err.message === "string" && err.message.trim() !== "") {
    return err.message;
  }

  return null;
}

export const useLogin = (): UseLoginReturn => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // user: 최초 1회만 localStorage에서 읽어 초기화
  const [user, setUser] = useState<UserInfo | null>(() => {
    const s = localStorage.getItem("userInfo");
    if (!s) return null;
    try {
      return JSON.parse(s) as UserInfo;
    } catch {
      return null;
    }
  });

  const submit = useCallback(
    async (e?: React.FormEvent) => {
      if (e) e.preventDefault();
      if (!username.trim() || !password.trim()) {
        setError("아이디와 비밀번호를 모두 입력해주세요.");
        return;
      }
      if (loading) return; // 중복 제출 방지

      setLoading(true);
      setError(null);

      try {
        // loginApi가 타입이 없다면 아래 한 줄로 명시 가능:
        // const res = await (loginApi as (p: { username: string; password: string }) => Promise<LoginResult>)({ username, password });
        const res = (await loginApi({ username, password })) as LoginResult;

        localStorage.setItem("accessToken", res.accessToken);
        localStorage.setItem("refreshToken", res.refreshToken);

        const userInfo: UserInfo = {
          userId: res.userId,
          username: res.username,
          name: res.name,
          role: res.role,
        };
        localStorage.setItem("userInfo", JSON.stringify(userInfo));
        setUser(userInfo);

        navigate("/main");
      } catch (err: unknown) {
        // setError(pickErrorMessage(err) ?? "로그인에 실패했습니다.");
        const maybeAxios = err as any;
        const status = maybeAxios?.response?.status as number | undefined;

        if (status === 401 || status === 400) {
          setError("아이디 또는 비밀번호가 올바르지 않습니다.");
        } else if (!maybeAxios?.response) {
          setError("네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.");
        } else {
          setError(pickErrorMessage(err) ?? "로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
      } finally {
        setLoading(false);
      }
    },
    [username, password, loading, navigate]
  );

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userInfo");
    setUser(null);
    navigate("/");
  }, [navigate]);

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
