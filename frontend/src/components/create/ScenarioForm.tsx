/** @format */

import { useShallow } from "zustand/react/shallow";

import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import TextButton from "../TextButton";
import TextInput from "../TextInput";
import boyHead from "../../assets/scenarios/cinema/cinema-boy-head.png";

interface ScenarioFormProps {
  onNext: () => void;
}

export default function ScenarioForm({ onNext }: ScenarioFormProps) {
  // 리렌더링 방지 위한 selector + shallow pattern
  // TODO: onChange 방식으로 관리 시, 매번 모든 전역스토어 리렌더링 문제?
  // 이렇게 관리하는게 잘한건지 몰르겠슈
  const { title, summary, categoryId, thumbnail, background } =
    useScenarioCreateStore(
      useShallow((s) => ({
        title: s.title,
        summary: s.summary,
        categoryId: s.categoryId,
        thumbnail: s.thumbnail,
        background: s.background,
      }))
    );

  const setScenarioInfo = useScenarioCreateStore((s) => s.setScenarioInfo);
  const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    // e.target.value는 항상 문자열
    const newCategoryId = parseInt(e.target.value, 10);
    setScenarioInfo({ categoryId: newCategoryId });
  };

  const handleFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    fieldName: "thumbnail" | "background"
  ) => {
    // fileList 객체 첫 번째만 가져오기
    const file = e.target.files ? e.target.files[0] : null;
    setScenarioInfo({ [fieldName]: file });
  };

  const handleClearFile = (fieldName: "thumbnail" | "background") => {
    // store 초기화
    setScenarioInfo({ [fieldName]: null });
  };

  //   미리보기
  const thumbnailUrl = thumbnail ? URL.createObjectURL(thumbnail) : null;

  return (
    <div className="w-full">
      <div className="flex flex-row gap-5">
        {/* 메타데이터 입력 */}
        <div className="flex-1 bg-pink-50 rounded-2xl p-8 shadow-sm">
          <h2 className="text-lg font-bold mb-4">시나리오 정보 입력하기</h2>

          <div className="flex items-center gap-3 mb-4">
            {/* TODO: 카테고리 불러오기*/}
            <p className="w-24 text-md">카테고리</p>
            <select
              name="category"
              id="scenario-category"
              value={categoryId}
              onChange={handleCategoryChange}
              className="flex-1 border border-gray-300 rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-pink-300"
            >
              <option value={1}>카테1</option>
              <option value={2}>카테2</option>
              <option value={3}>카테3</option>
            </select>
          </div>
          <div className="space-y-4 mb-4">
            <TextInput
              name="title"
              placeholder="시나리오 제목을 입력하세요"
              value={title}
              onChange={(val) => setScenarioInfo({ title: val })}
            >
              시나리오 제목
            </TextInput>
            <TextInput
              name="summary"
              value={summary}
              placeholder="시나리오 설명을 입력하세요."
              onChange={(val) => setScenarioInfo({ summary: val })}
            >
              시나리오 설명
            </TextInput>
          </div>
          <div className="flex items-center gap-3 mb-3">
            <p>썸네일 이미지</p>
            <input
              type="file"
              name="thumbnailImg"
              accept="image/*"
              onChange={(e) => handleFileChange(e, "thumbnail")}
              className="text-sm text-gray-600"
            />

            {/* 파일 생겼을 때만 표시&삭제 */}
            {thumbnail && (
              <div className="text-sm flex items-center gap-2">
                <span>파일: {thumbnail.name} </span>
                <button
                  type="button"
                  onClick={() => handleClearFile("thumbnail")}
                  className="text-red-500 hover:text-red-600 font-bold"
                >
                  X
                </button>
              </div>
            )}
          </div>
          <div className="flex flex-row items-center gap-3 mb-4">
            <p>배경화면 이미지</p>
            <input
              type="file"
              name="backgroundImg"
              accept="image/*"
              onChange={(e) => handleFileChange(e, "background")}
              className="text-sm text-gray-600"
            />
            {background && (
              <div className="text-sm flex items-center gap-2">
                <span>파일: {background.name}</span>
                <button
                  type="button"
                  onClick={() => handleClearFile("background")}
                  className="text-red-500 hover:text-red-600 font-bold"
                >
                  X
                </button>
              </div>
            )}
          </div>
        </div>

        {/* 썸네일 미리보기 */}
        <div className="flex-1 bg-pink-50 rounded-2xl p-6 shadow-sm flex flex-col items-center justify-center text-center">
          <div className="flex flex-row items-center gap-3 mb-4">
            <div className="h-20 flex-shrink-0">
              <img
                src={boyHead}
                alt="미리보기 캐릭터"
                className="w-full h-full object-contain"
              />
            </div>
            <div className="flex flex-col items-start text-left">
              <h2 className="text-xl font-bold leading-tight">
                썸네일 미리보기
              </h2>
              <p className="text-gray-600 text-sm">화면은 이렇게 만들어져요</p>
            </div>
          </div>

          {/* 썸네일 박스 유지 */}
          <div
            className={`
      w-80 h-56 rounded-xl border-2 border-dashed 
      flex items-center justify-center 
      bg-white transition-all
      ${thumbnailUrl ? "border-gray-200" : "border-gray-300"}
    `}
          >
            {thumbnailUrl ? (
              <img
                src={thumbnailUrl}
                alt="썸네일 미리보기"
                className="object-cover w-full h-full rounded-xl"
              />
            ) : (
              <div className="flex flex-col items-center justify-center text-gray-400">
                <p className="text-sm">대표 이미지를 추가하세요</p>
              </div>
            )}
          </div>
        </div>
      </div>
      <div className="flex flex-row justify-between mt-6">
        <TextButton>취소</TextButton>
        <TextButton onClick={onNext}>다음</TextButton>
      </div>
    </div>
  );
}
