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

/** 달력 일일 데이터 */
export interface CalendarDayData {
  day: number;
  studentActivities: StudentActivitySummary[];
}

/** 학생 활동 요약 */
export interface StudentActivitySummary {
  studentId: number;
  studentName: string;
  sessionCount: number;
}

/** 월별 달력 응답 */
export interface CalendarMonthlyResponse {
  year: number;
  month: number;
  dailyData: CalendarDayData[];
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
