/** @format */

type EndScreenProps = {
  onRestart: () => void;
};

export default function EndScreen({ onRestart }: EndScreenProps) {
  return (
    <div>
      <p>시나리오 완료</p>
      <button onClick={onRestart}>다시 하기</button>
    </div>
  );
}
