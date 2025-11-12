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
    <div
      className={`flex items-center gap-3 px-5 py-2 rounded-xl cursor-pointer transition-all 
        ${
          isActive
            ? "bg-pink-300 text-white font-semibold"
            : "bg-pink-50 text-gray-800 hover:bg-pink-100"
        }`}
    >
      <button onClick={onClick} className="flex-1">
        <span className="px-3">질문답변 {num}</span>
      </button>
      <button onClick={onDelete} className="text-red-500 hover:text-red-700">
        X
      </button>
    </div>
  );
}
