/** @format */

interface FeedbackScreenProps {
  isCorrect: boolean;
}

/**
 * 정/오답 여부 출력 화면 - feedback 시 이전 화면에서 overlay
 */
export default function FeedbackScreen({ isCorrect }: FeedbackScreenProps) {
  const message = isCorrect ? '정답' : '한번 더';
  const color = isCorrect
    ? 'text-blue-500 border-blue-500'
    : 'text-emerald-500 border-emerald-red500';
  const symbol = isCorrect ? 'O' : '✔';

  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm z-50">
      {/* 원형 또는 체크 표시 */}
      <div className={`text-[300px] mb-8 leading-none ${color}`}>{symbol}</div>

      {/* 캐릭터 이미지 */}

      {/* 텍스트 */}
      <p className={`text-8xl font-bold ${color}`}>{message}</p>
    </div>
  );
}
