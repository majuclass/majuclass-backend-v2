/** @format */

import api from "./apiInstance";

/** 학생 목록 조회 */
export const getStudents = async () => {
  const response = await api.get("/students");
  return response.data.data;
};

/** 학생 상세 조회 */
export const getStudent = async (studentId: number) => {
  const response = await api.get(`/students/${studentId}`);
  return response.data.data;
};

/** 학생 추가 */
export const createStudent = async (name: string) => {
  const response = await api.post("/students/create", { name });
  return response.data.data;
};

/** 학생 정보 수정 */
export const updateStudent = async (
  studentId: number,
  data: { name?: string; userId?: number }
) => {
  const response = await api.put(`/students/update/${studentId}`, data);
  return response.data.data;
};

/** 학생 삭제 */
export const deleteStudent = async (studentId: number) => {
  const response = await api.delete(`/students/delete/${studentId}`);
  return response.data;
};

/** 월별 달력 데이터 조회 */
export const getMonthlyCalendar = async (year: number, month: number) => {
  const response = await api.get("/students/calendar/monthly", {
    params: { year, month },
  });
  return response.data.data;
};

/** 특정 날짜의 학생 세션 목록 조회 */
export const getDailySessions = async (studentId: number, date: string) => {
  const response = await api.get("/students/calendar/daily-sessions", {
    params: { studentId, date },
  });
  return response.data.data;
};
