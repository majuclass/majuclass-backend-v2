/** @format
 * feedback 시 이전 화면에서 overlay
 */
interface FeedbackScreenProps {
  isCorrect: boolean;
}

export default function FeedbackScreen({ isCorrect }: FeedbackScreenProps) {
  const message = isCorrect ? "정답" : "오답";
  return (
    <div>
      <p>{message}</p>
    </div>
  );
}
