import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../apis/apiInstance';
import '../styles/ScenarioAiCreatePage.css';

interface ScenarioGeneratorProps {
  onGenerate?: (categoryId: number, questionCount: number, exampleCount: number, description: string) => void;
}

type Category = {
  id: number;
  categoryName: string;
};

// 카테고리 조회 API
const fetchCategories = async () => {
  const resp = await api.get('categories');
  return resp.data.data as Category[];
};

const ScenarioGenerator: React.FC<ScenarioGeneratorProps> = ({ onGenerate }) => {
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [questionCount, setQuestionCount] = useState<number>(5);
  const [exampleCount, setExampleCount] = useState<number>(3);
  const [description, setDescription] = useState<string>('');

  // 카테고리 조회
  const {
    data: categories,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 1000 * 60 * 60 * 24, // 24시간
  });

  const categoryList = categories || [];

  const handleGenerate = () => {
    if (description.trim() && categoryId !== null) {
      onGenerate?.(categoryId, questionCount, exampleCount, description);
      // API 호출 예시
      console.log({
        category_id: categoryId,
        seq_cnt: questionCount,
        option_cnt: exampleCount,
        prompt: description,
      });
    }
  };

  if (isLoading) {
    return (
      <div className="scenario-generator">
        <div className="generator-container">
          <p className="loading-text">카테고리 정보 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="scenario-generator">
        <div className="generator-container">
          <p className="error-text">카테고리 로드 중 에러 발생</p>
        </div>
      </div>
    );
  }

  return (
    <div className="scenario-generator">
      <h1 className="generator-title">AI 시나리오 생성 도우미</h1>
      
      <div className="generator-container">
        <div className="generator-controls">
          <div className="control-group">
            <label className="control-label">카테고리</label>
            <div className="control-input-wrapper">
              <select
                className="control-select category-select"
                value={categoryId ?? ''}
                onChange={(e) => setCategoryId(Number(e.target.value))}
              >
                <option value="">선택하세요</option>
                {categoryList.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.categoryName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="control-group">
            <label className="control-label">질문 개수</label>
            <div className="control-input-wrapper">
              <select
                className="control-select"
                value={questionCount}
                onChange={(e) => setQuestionCount(Number(e.target.value))}
              >
                {[1, 2, 3, 4, 5, 6, 7].map(num => (
                  <option key={num} value={num}>{num}</option>
                ))}
              </select>
              <span className="control-unit">개</span>
            </div>
          </div>

          <div className="control-group">
            <label className="control-label">보기 개수</label>
            <div className="control-input-wrapper">
              <select
                className="control-select"
                value={exampleCount}
                onChange={(e) => setExampleCount(Number(e.target.value))}
              >
                {[2, 3, 4].map(num => (
                  <option key={num} value={num}>{num}</option>
                ))}
              </select>
              <span className="control-unit">개</span>
            </div>
          </div>
        </div>

        <div className="description-section">
          <h2 className="description-title">시나리오 설명</h2>
          <div className="description-box">
            <textarea
              className="description-textarea"
              placeholder="예) '영화관에서 팝콘 주문하기' 상황을 연습하고 싶어요.
- 대상: 초등 고학년 
- 포커스: 인사하기, 주문하기"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={6}
            />
          </div>
        </div>

        <div className="generate-button-wrapper">
          <button
            className="generate-button"
            onClick={handleGenerate}
            disabled={!description.trim() || categoryId === null}
          >
            AI 시나리오 생성
          </button>
        </div>
      </div>
    </div>
  );
};

export default ScenarioGenerator;
