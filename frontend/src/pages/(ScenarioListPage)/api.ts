// src/pages/(ScenarioListPage)/api.ts
/* eslint-disable @typescript-eslint/no-explicit-any */
import axios from "axios";

// ===== 공통 타입 =====
export type Difficulty = "EASY" | "HARD";

export interface Scenario {
  id: number;
  title: string;
  summary: string;
  thumbnailUrl?: string;
  categoryId?: number;
  categoryName?: string;
  totalSequences?: number;
  difficulty?: Difficulty;
  createdAt?: string; // ISO
  updatedAt?: string; // ISO
}

// ✅ 스웨거 공통 래퍼 (실제 응답 형태에 맞춤)
interface ApiResponse<T> {
  status: "SUCCESS" | "ERROR";
  message?: string;
  data: T;
}

// ✅ .env 사용 (끝 슬래시 제거)
const BASE_URL =
  import.meta.env.VITE_BASE_API_URL?.replace(/\/+$/, "") || "";

// ✅ axios 인스턴스 (시나리오 페이지 전용)
const scenarioApi = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
});

// ✅ 요청 인터셉터 - 토큰 자동 주입
scenarioApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers = config.headers ?? {};
    (config.headers as any).Authorization = `Bearer ${token}`;
  }
  return config;
});

// ✅ 공통 언랩
function unwrap<T>(res: ApiResponse<T>): T {
  if (res.status !== "SUCCESS") {
    throw new Error(res.message ?? "요청에 실패했습니다.");
  }
  return res.data;
}

// ===== API: 시나리오 목록 =====
export interface FetchScenariosParams {
  categoryId?: number;
  difficulty?: Difficulty;
  // 백엔드 스웨거에는 page/size 없어서 옵션으로만 두고, 있으면 보냄
  page?: number;
  size?: number;
}

export async function fetchScenarios(
  params?: FetchScenariosParams
): Promise<Scenario[]> {
  // 스웨거 기준 엔드포인트: GET /scenarios
  const query: Record<string, string> = {};

  if (typeof params?.categoryId === "number") {
    query.categoryId = String(params.categoryId);
  }
  if (params?.difficulty) {
    query.difficulty = params.difficulty;
  }
  if (typeof params?.page === "number") {
    query.page = String(params.page);
  }
  if (typeof params?.size === "number") {
    query.size = String(params.size);
  }

  const { data } = await scenarioApi.get<ApiResponse<Scenario[]>>("/scenarios", {
    params: query,
  });

  return unwrap(data) ?? [];
}

// ===== API: 시나리오 단건 상세 =====
export async function fetchScenarioById(
  scenarioId: number
): Promise<Scenario> {
  // 스웨거: GET /scenarios/{scenarioId}
  const { data } = await scenarioApi.get<ApiResponse<Scenario>>(
    `/scenarios/${scenarioId}`
  );
  return unwrap(data);
}
