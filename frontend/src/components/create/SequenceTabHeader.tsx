/** @format */
interface SequenceTabHeaderProps {
  num: number | undefined;
  isActive: boolean;
  onClick: () => void;
  onDelete: () => void;
}

export default function SequenceTabHeader({
  num,
  isActive,
  onClick,
  onDelete,
}: SequenceTabHeaderProps) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 px-5 py-2 rounded-xl cursor-pointer transition-all 
        ${
          isActive
            ? "bg-pink-300 text-white font-semibold"
            : "bg-pink-50 text-gray-800 hover:bg-pink-100"
        }`}
    >
      <span>질문답변 {num}</span>
      <button
        onClick={(e) => {
          e.stopPropagation(); // 클릭 전파 방지
          onDelete();
        }}
        className="hover:text-gray-700"
      >
        X
      </button>
    </button>
  );
}
