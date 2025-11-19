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

/** 시나리오 옵션 단일 조회
 * 난이도에 따라 optionText | optionImageUrl 2가지 중 하나만 내려옴
 */
export interface Option {
  optionId: number;
  optionNumber: number;
  optionText?: string;
  optionImageUrl?: string;
  // 지금 내려오는 값이 isAnswer랑 answer랑 필드가 2가지..?
  isAnswer?: boolean;
  answer?: boolean;
}

/** 프론트엔드 UI용 옵션 구조 */
export interface TransformedOption {
  id: number;
  number: number;
  label: string; // optionText or optionImageUrl
  isAnswer: boolean; // answer or isAnswer
  type: "text" | "image"; // option Type
}
