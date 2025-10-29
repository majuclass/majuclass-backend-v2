// hooks/useAuth.ts

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface LoginPayload {
  username: string;
  password: string;
  rememberMe: boolean;
}

interface LoginResponse {
  status: 'SUCCESS' | 'ERROR';
  message: string;
  data?: {
    userId: number;
    username: string;
    name: string;
    role: string;
    accessToken: string;
    refreshToken: string;
  };
}

interface AuthState {
  isLoading: boolean;
  error: string | null;
  user: {
    userId: number;
    username: string;
    name: string;
    role: string;
  } | null;
}

// const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const API_BASE_URL = 'http://localhost:8080'

export const useAuth = () => {
  const navigate = useNavigate();
  const [authState, setAuthState] = useState<AuthState>({
    isLoading: false,
    error: null,
    user: null
  });

  // 로그인 함수
  const login = async (payload: LoginPayload) => {
    setAuthState(prev => ({ ...prev, isLoading: true, error: null }));

    try {
      // API 호출
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: payload.username,
          password: payload.password
        })
      });

      const data: LoginResponse = await response.json();

      // 응답 상태 확인
      if (!response.ok || data.status !== 'SUCCESS') {
        throw new Error(data.message || '로그인에 실패했습니다.');
      }

      // 로그인 성공 처리
      if (data.data) {
        // 토큰 저장
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        
        // 사용자 정보 저장
        const userInfo = {
          userId: data.data.userId,
          username: data.data.username,
          name: data.data.name,
          role: data.data.role
        };
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
        
        // 상태 업데이트
        setAuthState({
          isLoading: false,
          error: null,
          user: userInfo
        });

        // 메인 페이지로 이동
        navigate('/main');
        
        return { success: true, user: userInfo };
      } else {
        throw new Error('로그인 응답 데이터가 올바르지 않습니다.');
      }
      
    } catch (error) {
      // 에러 처리
      let errorMessage = '로그인 중 오류가 발생했습니다.';
      
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      
      setAuthState(prev => ({
        ...prev,
        isLoading: false,
        error: errorMessage
      }));
      
      // 에러를 상위 컴포넌트로 전파
      throw new Error(errorMessage);
    }
  };

  // 로그아웃 함수
  const logout = () => {
    // 토큰 및 사용자 정보 삭제
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userInfo');
    
    // 상태 초기화
    setAuthState({
      isLoading: false,
      error: null,
      user: null
    });
    
    // 로그인 페이지로 이동
    navigate('/');
  };

  // 토큰 확인 함수
  const checkAuth = (): boolean => {
    const token = localStorage.getItem('accessToken');
    return !!token;
  };

  // 저장된 사용자 정보 가져오기
  const getCurrentUser = () => {
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        return JSON.parse(userInfoStr);
      } catch {
        return null;
      }
    }
    return null;
  };

  // Access Token 가져오기 (API 요청 시 사용)
  const getAccessToken = (): string | null => {
    return localStorage.getItem('accessToken');
  };

  // Refresh Token 가져오기
  const getRefreshToken = (): string | null => {
    return localStorage.getItem('refreshToken');
  };

  return {
    // 상태
    isLoading: authState.isLoading,
    error: authState.error,
    user: authState.user || getCurrentUser(),
    
    // 함수들
    login,
    logout,
    checkAuth,
    isAuthenticated: checkAuth(),
    getAccessToken,
    getRefreshToken,
    getCurrentUser
  };
};