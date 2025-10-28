/** @format */

type EndScreenProps = {
  onRestart: () => void;
};

export default function EndScreen({ onRestart }: EndScreenProps) {
  return (
    <div className="h-screen w-screen flex items-center justify-center bg-[url('/snack-bg.jpg')] bg-cover bg-center">
      <div className="backdrop-blur-sm bg-white/80 rounded-2xl shadow-xl p-10 text-center w-80">
        <h2 className="text-2xl">시나리오 완료</h2>
        <button
          className="bg-orange-400 text-white py-2 px-6 rounded-md font-semibold hover:bg-orange-500 transition"
          onClick={onRestart}
        >
          다시하기
        </button>
      </div>
    </div>
  );
}
