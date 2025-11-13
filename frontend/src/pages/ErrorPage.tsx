// ErrorPage.tsx (NotFoundPage를 대체)
// react Router가 일반적인 리액트 에러 경계보다 '먼저' 에러를 가로챔

// -> NotFoundPage를 범용 ErrorPage로 개선하기
import { useRouteError, isRouteErrorResponse } from 'react-router-dom';

export default function ErrorPage() {
  const error = useRouteError(); // 오류 객체 가져오기
  let title = '오류 발생';
  let message = '예상치 못한 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.';

  // 1. 라우트 에러 응답 (HTTP 상태 코드, 예: 404, 401, 500)
  if (isRouteErrorResponse(error)) {
    title = `오류 ${error.status}`;

    if (error.status === 404) {
      message = '요청하신 페이지를 찾을 수 없습니다. (404 Not Found)';
    } else if (error.status === 401) {
      message = '접근 권한이 없습니다. 로그인 상태를 확인해 주세요.';
    } else {
      message = error.statusText || '서버 응답 오류가 발생했습니다.';
    }
  }

  // 2. 일반 JavaScript Error (렌더링 에러)
  else if (error instanceof Error) {
    title = '애플리케이션 오류';
    // 개발자용: console.error(error.message);
    message =
      '페이지를 로드하는 중 오류가 발생했습니다. 개발자에게 제보해주세요 ㅜ.ㅜ';
  }

  // 🌟 2. NGINX 200 환경의 404 처리 (path: "*" 라우트에 의해 렌더링됨)
  else {
    // error 객체가 없고, path: "*" 라우트에 의해 렌더링되었다면 404일 가능성이 매우 높음
    title = '404 Not Found';
    message = `요청하신 경로 (${location.pathname})에 해당하는 페이지를 찾을 수 없습니다. 잘못 들어오셨나봐요!`;
  }

  // 3. 그 외 알 수 없는 오류
  // title, message는 초기값 유지

  return (
    <div style={{ padding: '40px', textAlign: 'center', color: 'red' }}>
      <h1 className="font-bold">{title}</h1>
      <p className="py-10">{message}</p>
      {/* 🌟 사용자에게 친절하게 메인 페이지로 돌아가는 버튼 제공 */}
      <a href="/">메인으로 돌아가기</a>

      {/* 개발 단계에서만 에러 상세 정보를 보여줍니다. */}
      {/* {import.meta.env.DEV && error && <pre>{JSON.stringify(error, null, 2)}</pre>} */}
    </div>
  );
}
