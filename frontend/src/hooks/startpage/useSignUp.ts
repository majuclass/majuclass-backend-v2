/** @format */

import { useState } from 'react';
import { signUp, type SignUpRequest } from '../../apis/signUpApi';
import axios from 'axios';

export const useSignUp = (onSuccess?: () => void) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    // 비밀번호 확인 검증
    if (password !== passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }

    setLoading(true);

    try {
      const requestData: SignUpRequest = {
        username: username.trim(),
        email: email.trim(),
        name: name.trim(),
        password, // 비밀번호는 trim 하지 않음 (공백도 유효한 문자)
        schoolId: 1, // 임시: 기본 학교 ID (백엔드 필수 요구 가능성)
      };

      console.log('회원가입 요청 데이터:', requestData); // 디버깅용

      await signUp(requestData);

      // 성공 시 로그인 카드로 전환
      alert('회원가입이 완료되었습니다! 로그인해주세요.');
      if (onSuccess) {
        onSuccess();
      }
    } catch (err) {
      console.error('회원가입 오류:', err); // 디버깅용
      if (axios.isAxiosError(err)) {
        console.error('서버 응답:', err.response?.data); // 디버깅용
        const errorMessage =
          err.response?.data?.message || '회원가입에 실패했습니다.';
        setError(errorMessage);
      } else if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('알 수 없는 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  return {
    username,
    setUsername,
    email,
    setEmail,
    name,
    setName,
    password,
    setPassword,
    passwordConfirm,
    setPasswordConfirm,
    loading,
    error,
    submit,
  };
};
