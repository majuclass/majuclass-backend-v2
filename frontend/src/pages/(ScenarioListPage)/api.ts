// src/pages/(ScenListPage)/api.ts

// ===== 공통 타입 =====
export type Difficulty = 'EASY' | 'HARD';

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

// 백엔드 공통 래퍼 (스웨거: ApiResponse)
interface ApiResponse<T> {
  code: number | string;     // e.g. 200 or "SUCCESS"
  message: string;
  data: T;
}

const API_BASE = '/api';

// ===== 내부 유틸 =====
function authHeader(): Record<string, string> {
  const token = localStorage.getItem('accessToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function fetchJSON<T>(input: RequestInfo, init?: RequestInit): Promise<T> {
  const res = await fetch(input, init);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status} ${res.statusText} :: ${text}`);
  }
  return (await res.json()) as T;
}

// ===== API: 시나리오 목록 =====
export interface FetchScenariosParams {
  categoryId?: number;
  difficulty?: Difficulty;
  page?: number; // 0-based
  size?: number; // page size
}

export async function fetchScenarios(
  params?: FetchScenariosParams,
  signal?: AbortSignal
): Promise<Scenario[]> {
  const q = new URLSearchParams();
  if (params?.categoryId) q.set('categoryId', String(params.categoryId));
  if (params?.difficulty) q.set('difficulty', params.difficulty);
  
  if (typeof params?.page === 'number') q.set('page', String(params.page));
  if (typeof params?.size === 'number') q.set('size', String(params.size));

  const url = `${API_BASE}/scenarios${q.toString() ? `?${q.toString()}` : ''}`;

  const resp = await fetchJSON<ApiResponse<Scenario[]>>(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeader(),
    },
    signal,
  });

  // 성공/실패 판단은 백 코드 정책에 따라 다를 수 있음. 여기서는 data만 신뢰해서 반환.
  return resp.data ?? [];
}

// ===== API: 시나리오 단건 상세 =====
export async function fetchScenarioById(
  scenarioId: number,
  signal?: AbortSignal
): Promise<Scenario> {
  const url = `${API_BASE}/scenarios/${scenarioId}`;

  const resp = await fetchJSON<ApiResponse<Scenario>>(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...authHeader(),
    },
    signal,
  });

  return resp.data;
}
