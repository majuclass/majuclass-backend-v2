import NavBar from '../components/NavBar';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50">
      <NavBar />

      <div className="flex items-center justify-center px-6 py-20">
        <div className="text-center max-w-md">
          {/* 이모지/아이콘 */}
          <div className="text-8xl mb-6 animate-bounce">🔍</div>

          {/* 404 숫자 */}
          <div className="text-6xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-pink-500 to-purple-600 mb-4 font-nsrExtraBold">
            404
          </div>

          {/* 메시지 */}
          <div className="bg-white rounded-3xl shadow-lg p-8 mb-6">
            <p className="text-2xl font-bold text-gray-800 mb-3 font-nsrExtraBold">
              잘못된 페이지
            </p>
            <p className="text-gray-600 text-lg">
              페이지를 찾을 수 없어요.
              <br />
              잘못된 주소로 접속한 것 같아요!
            </p>
          </div>

          {/* 홈으로 버튼 */}
          <button
            onClick={() => (window.location.href = '/main')}
            className="px-8 py-4 font-nsrExtraBold bg-gradient-to-r from-pink-400 to-pink-500 hover:from-pink-500 hover:to-pink-600 text-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105"
          >
            <span className="text-lg">🏠 홈으로 돌아가기</span>
          </button>
        </div>
      </div>
    </div>
  );
}
