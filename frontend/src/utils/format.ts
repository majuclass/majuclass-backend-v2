/** @format 포맷팅 관련 유틸리티 함수 */

import type { Option, TransformedOption } from "../types/Scenario";

/** option 난이도 변경에 따라 리턴받는 값 조절
 * 난이도 EASY - optionImageUrl
 * 난이도 NORMAL - optionText
 * @param {('EASY' | 'NORMAL')} difficulty - 현재 난이도
 * @param {Array<Object>} rawOptions - 백엔드에서 받은 원본 옵션 데이터
 * @returns {Array<Object>} 통일 구조로 변환된 옵션 데이터
 */
export const transformOptions = (
  difficulty: string,
  rawOptions: Option[]
): TransformedOption[] => {
  if (!rawOptions) return [];

  return rawOptions.map((option) => {
    // 백엔드에서 어떤건 isAnswer고 어떤건 answer라서 ...
    const unifiedIsAnswer = option.isAnswer ?? option.answer ?? false;

    let label = "";
    let type = ""; // img, text 2개만 존재

    if (difficulty === "EASY" && option.optionImageUrl) {
      label = option.optionImageUrl;
      type = "image";
    } else if (difficulty === "NORMAL" && option.optionText) {
      label = option.optionText;
      type = "text";
    } else if (difficulty === "HARD" && option.optionText) {
      label = option.optionText;
      type = "text";
    }

    return {
      id: option.optionId,
      number: option.optionNumber,
      isAnswer: unifiedIsAnswer,
      label: label,
      type: type,
    } as TransformedOption;
  });
};
