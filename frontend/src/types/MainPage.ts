/** @format */

/** 학생 응답 */
export interface StudentResponse {
  studentId: number;
  name: string;
  userId: number;
  userName: string;
  schoolName: string;
  createdAt: string;
  updatedAt: string;
}

/** 학생 생성 요청 */
export interface StudentCreateRequest {
  name: string;
}

/** 학생 수정 요청 */
export interface StudentUpdateRequest {
  name?: string;
  userId?: number;
}

/** 학생 세션 카운트 (백엔드 스펙) */
export interface StudentSessionCountDto {
  studentId: number;
  studentName: string;
  sessionCount: number;
}

/** 달력 일일 통계 (백엔드 스펙) */
export interface CalendarDayStatsDto {
  date: string; // "2025-01-15" 형태
  studentSessions: StudentSessionCountDto[];
  totalSessionCount: number;
}

/** 월별 달력 응답 (백엔드 스펙) */
export interface CalendarMonthlyResponse {
  year: number;
  month: number;
  dailyStats: CalendarDayStatsDto[];
  totalDays: number;
}

/** 일일 세션 아이템 */
export interface DailySessionItem {
  sessionId: number;
  scenarioId: number;
  scenarioTitle: string;
  thumbnailUrl: string;
  categoryName: string;
  status: "IN_PROGRESS" | "COMPLETED" | "ABORTED";
  createdAt: string;
  completedAt?: string;
}

/** 일일 세션 목록 응답 */
export interface DailySessionListResponse {
  studentId: number;
  studentName: string;
  date: string;
  sessions: DailySessionItem[];
  totalCount: number;
}
