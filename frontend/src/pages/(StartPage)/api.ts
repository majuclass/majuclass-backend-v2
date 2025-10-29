/* eslint-disable @typescript-eslint/no-explicit-any */
// src/pages/(StartPage)/api.ts
import axios from "axios";

// 고정 BASE_URL (차후 ENV 전환 가능)
export const api = axios.create({
  baseURL: "http://k13a202.p.ssafy.io:8080/api",
  timeout: 15000,
});

// 요청 인터셉터 - 토큰 자동 주입
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers = config.headers ?? {};
    (config.headers as any).Authorization = `Bearer ${token}`;
  }
  return config;
});

// 공통 응답 타입
type ApiResponse<T> = {
  status: "SUCCESS" | "ERROR";
  message?: string;
  data: T;
};

// 로그인 요청/응답 타입
export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  userId: number;
  username: string;
  name: string;
  role: string;
  accessToken: string;
  refreshToken: string;
};

// 공통 언랩 함수 (응답 상태 검증)
function unwrap<T>(res: ApiResponse<T>): T {
  if (res.status !== "SUCCESS") {
    throw new Error(res.message ?? "요청에 실패했습니다.");
  }
  return res.data;
}

// 로그인 API
export async function login(req: LoginRequest): Promise<LoginResponse> {
  const { data } = await api.post<ApiResponse<LoginResponse>>("/auth/login", req);
  return unwrap(data);
}
