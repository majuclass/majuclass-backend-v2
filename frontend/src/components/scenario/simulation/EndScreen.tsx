/** @format */

import { useNavigate } from "react-router-dom";

type EndScreenProps = {
  onRestart: () => void;
  onExit: () => void;
};

export default function EndScreen({ onRestart, onExit }: EndScreenProps) {
  const navigator = useNavigate();
  return (
    <div className="h-screen w-screen flex items-center justify-center bg-[url('/snack-bg.jpg')] bg-cover bg-center">
      <div className="backdrop-blur-sm bg-white/80 rounded-2xl shadow-xl p-12 text-center w-full max-w-xl align-center">
        <h2 className="text-2xl">시나리오 완료</h2>
        <div className="flex gap-5 justify-center">
          <button
            className="bg-orange-400 text-white py-2 px-6 rounded-md font-semibold hover:bg-orange-500 transition"
            onClick={onRestart}
          >
            다시하기
          </button>
          <button
            className="bg-gray-400 text-white py-2 px-6 rounded-md font-semibold hover:bg-gray-500 transition"
            onClick={() => {
              onExit();
              navigator(`/scenarios`);
            }}
          >
            나가기
          </button>
        </div>
      </div>
    </div>
  );
}
