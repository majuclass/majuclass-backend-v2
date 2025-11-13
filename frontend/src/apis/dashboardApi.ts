/** @format */

import api from "./apiInstance";

/** 카테고리별 통계 조회 (도넛 그래프용) */
export const getCategoryStats = async (
  studentId: number,
  year: number,
  month: number
) => {
  const response = await api.get(
    `/students/dashboard/category-stats/${studentId}`,
    {
      params: { year, month },
    }
  );
  return response.data.data;
};

/** 월별 세션 목록 조회 */
export const getMonthlySessions = async (
  studentId: number,
  year: number,
  month: number,
  categoryId?: number,
  status?: "IN_PROGRESS" | "COMPLETED" | "ABORTED"
) => {
  const response = await api.get(
    `/students/dashboard/monthly-sessions/${studentId}`,
    {
      params: { year, month, categoryId, status },
    }
  );
  return response.data.data;
};

/** 세션 시퀀스별 통계 조회 (정답률) */
export const getSessionSequenceStats = async (sessionId: number) => {
  const response = await api.get(
    `/students/dashboard/sequence-stats/${sessionId}`
  );
  return response.data.data;
};

/** 세션 전체 음성 답변 조회 */
export const getAudioAnswers = async (sessionId: number) => {
  const response = await api.get(
    `/scenario-sessions/audio-answers/${sessionId}`
  );
  return response.data.data;
};
