/* @format */

import { fastapi } from "./apiInstance";

// import api from "./apiInstance";

/* AI 시나리오 자동 생성 Request */
export interface AIScenarioCreateRequest {
  category_id: number;
  seq_cnt: number;
  option_cnt: number;
  prompt: string;
}

/* AI 시나리오 자동 생성 Response */
export interface AIScenarioCreateResponse {
  title: string;
  summary: string;
  categoryId: number;
  thumbnailS3Key: string;
  backgroundS3Key: string;
  sequences: AISequenceResponse[];
}

export interface AISequenceResponse {
  seqNo: number;
  question: string;
  options: AIOptionResponse[];
}

export interface AIOptionResponse {
  optionNo: number;
  optionText: string;
  optionS3Key: string;
  isAnswer: boolean;
}

/* AI 시나리오 자동 생성 */
export const createAIScenario = async (
  request: AIScenarioCreateRequest
): Promise<AIScenarioCreateResponse> => {
  const response = await fastapi.post("/auto-create", request);
  return response.data.data;
};
