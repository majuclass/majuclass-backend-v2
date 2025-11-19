/** @format */

import api from './apiInstance';

export interface SignUpRequest {
  username: string;
  password: string;
  name: string;
  email: string;
  schoolId?: number;
}

export interface SignUpResponse {
  userId: number;
  username: string;
  name: string;
  email: string;
  role: string;
  schoolId: number | null;
}

/**
 * 회원가입 API
 * POST /auth/signup
 */
export const signUp = async (data: SignUpRequest): Promise<SignUpResponse> => {
  const response = await api.post('/auth/signup', data);

  if (response.data.status === 'SUCCESS') {
    return response.data.data;
  }

  throw new Error(response.data.message || '회원가입에 실패했습니다.');
};
