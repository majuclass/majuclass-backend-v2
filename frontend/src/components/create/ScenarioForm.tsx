/** @format */

import { useShallow } from "zustand/react/shallow";

import { useScenarioCreateStore } from "../../stores/useScenarioCreateStore";
import TextButton from "../TextButton";
import TextInput from "../TextInput";

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
    <>
      <h1>시나리오 생성</h1>
      <div className="flex flex-row ">
        {/* 메타데이터 입력 */}
        <div>
          <div>
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
          <div>
            {/* TODO: 카테고리 불러오기*/}
            <p>카테고리</p>
            <select
              name="category"
              id="scenario-category"
              value={categoryId}
              onChange={handleCategoryChange}
            >
              <option value={1}>카테1</option>
              <option value={2}>카테2</option>
              <option value={3}>카테3</option>
            </select>
          </div>
          {/* TODO: 이미지 preview 만들기 */}
          <div>
            <p>썸네일 이미지</p>
            <input
              type="file"
              name="thumbnailImg"
              accept="image/*"
              onChange={(e) => handleFileChange(e, "thumbnail")}
            />

            {/* 파일 생겼을 때만 표시&삭제 */}
            {thumbnail && (
              <div>
                <span>파일: {thumbnail.name} </span>
                <button
                  type="button"
                  onClick={() => handleClearFile("thumbnail")}
                >
                  X
                </button>
              </div>
            )}
          </div>
          <div>
            <p>배경화면 이미지</p>
            <input
              type="file"
              name="backgroundImg"
              accept="image/*"
              onChange={(e) => handleFileChange(e, "background")}
            />
            {background && (
              <div>
                <span>파일: {background.name}</span>
                <button
                  type="button"
                  onClick={() => handleClearFile("background")}
                >
                  X
                </button>
              </div>
            )}
          </div>
        </div>
        {/* 썸네일 미리보기 */}
        <div>
          <span>미리보기</span>
          {thumbnailUrl && <img src={thumbnailUrl} alt="썸네일 미리보기" />}
        </div>
      </div>
      <div className="flex flex-row justify-between">
        <TextButton>취소</TextButton>
        <TextButton onClick={onNext}>다음</TextButton>
      </div>
    </>
  );
}
