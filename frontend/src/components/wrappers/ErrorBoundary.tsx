import React, { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

// props 타입 정의
interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

// state 타입 정의
interface ErrorBoundaryState {
  hasError: boolean;
}

export default class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  // 초기 상태
  public state: ErrorBoundaryState = {
    hasError: false,
  };

  // 오류 발생 시 상태 업데이트
  public static getDerivedStateFromError(_: Error): ErrorBoundaryState {
    return { hasError: true };
  }

  // 오류 로깅
  public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('ErrorBoundary 오류 포착', error, errorInfo);
    // 외부 로깅 서비스로 전송 필요
  }

  // 렌더링
  public render(): ReactNode {
    if (this.state.hasError) {
      // fallback prop 있다면 렌더링, 없으면 기본 메시지 렌더링
      if (this.props.fallback) return this.props.fallback;

      return (
        <div style={{ padding: '20px', border: '1px solid red', color: 'red' }}>
          <h1>예상치 못한 문제가 발생했습니다!</h1>
          <p>개발자에게 제보해주세요 ㅜ.ㅜ</p>
        </div>
      );
    }

    // 오류 없으면 자식 컴포넌트 렌더링
    return this.props.children;
  }
}
