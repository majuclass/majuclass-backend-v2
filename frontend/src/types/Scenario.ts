/**
 * 시나리오에 사용되는 인터페이스 정의
 *
 * @format
 */

/** 시나리오 Base Interface */
interface ScenarioBase {
  title: string;
  summary: string;
  categoryId: number;
  totalSequences: number;
  difficulty: string;
}

/** 시나리오 받아오기 */
export interface GetScenario extends ScenarioBase {
  id: number;
  thumbnailUrl: string;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

/** 시나리오 생성 */
export interface CreateScenario extends ScenarioBase {
  thumbnailImg: File | null;
  backgroundImg: File | null;
}

/** 시나리오 시퀀스 단일 조회
 * hasNext?
 */
export interface Sequence {
  sequenceId: number;
  sequenceNumber: number;
  question: string;
  mediaUrl: string;
  options: Option[];
  hasNext: boolean;
}

/** 시나리오 옵션 단일 조회 */
export interface Option {
  optionId: number;
  optionNumber: number;
  optionText: string;
  isAnswer: boolean;
}
