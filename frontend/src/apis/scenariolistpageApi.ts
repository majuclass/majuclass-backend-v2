// src/pages/(ScenarioListPage)/api.ts
/* eslint-disable @typescript-eslint/no-explicit-any */

import api from "./apiInstance";   

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

// ✅ 스웨거 공통 래퍼
interface ApiResponse<T> {
  status: "SUCCESS" | "ERROR";
  message?: string;
  data: T;
}

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
  page?: number;
  size?: number;
}

export async function fetchScenarios(
  params?: FetchScenariosParams
): Promise<Scenario[]> {
  const { data } = await api.get<ApiResponse<Scenario[]>>("/scenarios", {
    params: {
      ...(typeof params?.categoryId === "number" && { categoryId: params.categoryId }),
      ...(params?.difficulty && { difficulty: params.difficulty }),
      ...(typeof params?.page === "number" && { page: params.page }),
      ...(typeof params?.size === "number" && { size: params.size }),
    },
  });
  return unwrap(data) ?? [];
}

// ===== API: 시나리오 단건 상세 =====
export async function fetchScenarioById(scenarioId: number): Promise<Scenario> {
  const { data } = await api.get<ApiResponse<Scenario>>(`/scenarios/${scenarioId}`);
  return unwrap(data);
}
