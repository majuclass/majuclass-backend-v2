/** @format */
interface SequenceTabHeaderProps {
  num: number | undefined;
  isActive: boolean;
  onClick: () => void;
}
export default function SequenceTabHeader({
  num,
  isActive,
  onClick,
}: SequenceTabHeaderProps) {
  return (
    <button
      onClick={onClick}
      className={isActive ? "bg-yellow-300 font-bold" : "bg-white"}
    >
      <span>질문답변 {num}</span>
    </button>
  );
}
