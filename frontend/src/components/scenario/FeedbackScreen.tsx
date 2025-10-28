/** @format
 * feedback 시 이전 화면에서 overlay
 */

interface FeedbackScreenProps {
  isCorrect: boolean;
}

export default function FeedbackScreen({ isCorrect }: FeedbackScreenProps) {
  const message = isCorrect ? "정답" : "한번 더";
  const color = isCorrect
    ? "text-blue-500 border-blue-500"
    : "text-red-500 border-red-500";
  // TODO: 더 찰진 방법 찾기
  const symbol = isCorrect ? "⭕" : "❓";

  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm z-50">
      {/* 원형 또는 체크 표시 */}
      <div className={`text-[250px] mb-8 leading-none ${color}`}>{symbol}</div>

      {/* 캐릭터 이미지 */}

      {/* 텍스트 */}
      <p className={`text-6xl font-bold ${color}`}>{message}</p>
    </div>
  );
}
