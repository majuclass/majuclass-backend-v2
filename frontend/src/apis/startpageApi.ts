/* eslint-disable @typescript-eslint/no-explicit-any */
// src/pages/(StartPage)/api.ts

import api from "./apiInstance"; 

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
