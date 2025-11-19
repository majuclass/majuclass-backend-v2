/** @format */
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ErrorBoundary from './components/wrappers/ErrorBoundary';
import router from './router';

// 전역 Provider들을 한 곳에 모아주는 wrapper
export default function App() {
  // tanstack query
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: 3 }, // 실패시 재시도 3회
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <RouterProvider router={router} />
      </ErrorBoundary>
    </QueryClientProvider>
  );
}
