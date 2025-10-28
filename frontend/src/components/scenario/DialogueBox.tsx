/** @format */

type DialogueBoxProps = {
  speaker: string;
  text: string;
  onNext?: () => void;
  showNextButton?: boolean;
};

export default function DialogueBox({
  speaker,
  text,
  onNext,
  showNextButton = false,
}: DialogueBoxProps) {
  return (
    <div className="bg-white rounded-xl shadow-xl w-11/12 max-w-md mb-10 p-4 border-4 border-b-8 border-gray-300">
      {/* 상단: 이름표 */}
      <div className="flex items-center mb-2">
        <div className="bg-yellow-200 font-bold px-2 py-1 rounded">
          {speaker}
        </div>
      </div>

      {/* 본문: 대사 */}
      <p className="text-gray-800 text-lg font-semibold whitespace-pre-line">
        {text}
      </p>

      {/* 하단: 다음 버튼 (옵션) */}
      {showNextButton && (
        <div className="flex justify-end mt-4">
          <button
            onClick={onNext}
            className="bg-orange-400 text-white font-bold py-1 px-4 rounded-md hover:bg-orange-500 transition"
          >
            답변하기
          </button>
        </div>
      )}
    </div>
  );
}
