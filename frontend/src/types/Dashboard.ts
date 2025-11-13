/** @format */

/** 카테고리별 통계 */
export interface CategoryStatsDto {
  categoryId: number;
  categoryName: string;
  sessionCount: number;
  percentage: number;
}

export interface CategoryStatsResponse {
  categoryStats: CategoryStatsDto[];
  totalSessions: number;
}

/** 세션 목록 */
export interface SessionListItemDto {
  sessionId: number;
  scenarioId: number;
  scenarioTitle: string;
  thumbnailUrl: string;
  categoryId: number;
  categoryName: string;
  status: "IN_PROGRESS" | "COMPLETED" | "ABORTED";
  createdAt: string;
}

export interface SessionsResponse {
  sessions: SessionListItemDto[];
  totalCount: number;
}

/** 시퀀스별 통계 */
export interface SequenceStatsDto {
  sequenceId: number;
  sequenceNumber: number;
  question: string;
  successAttempt: number;
  accuracyRate: number;
  isCorrect: boolean;
}

export interface SessionSequenceStatsResponse {
  sessionId: number;
  scenarioTitle: string;
  sequenceStats: SequenceStatsDto[];
  totalSequences: number;
  completedSequences: number;
  averageAccuracy: number;
}

/** 음성 답변 */
export interface AudioAnswerDto {
  answerId: number;
  attemptNo: number;
  audioUrl: string;
  isCorrect: boolean;
  createdAt: string;
}

/** 시퀀스별 음성 답변 */
export interface SequenceAudioAnswersDto {
  sequenceId: number;
  sequenceNumber: number;
  totalAttempts: number;
  audioAnswers: AudioAnswerDto[];
}

/** 세션 전체 음성 답변 응답 */
export interface AudioAnswerListResponse {
  sessionId: number;
  sequences: SequenceAudioAnswersDto[];
}
