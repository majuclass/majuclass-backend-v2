import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../apis/apiInstance';
import { createAIScenario, type AIScenarioCreateResponse } from '../apis/scenarioAiApi';
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

  // AI 생성 상태
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedScenario, setGeneratedScenario] = useState<AIScenarioCreateResponse | null>(null);

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

  const handleGenerate = async () => {
    if (!description.trim() || categoryId === null) {
      alert('카테고리와 시나리오 설명을 모두 입력해주세요.');
      return;
    }

    // 프롬프트 최소 길이 검증
    if (description.trim().length < 10) {
      alert('시나리오 설명을 최소 10자 이상 구체적으로 작성해주세요.\n\n예시: "영화관에서 팝콘을 주문하는 상황을 연습하고 싶어요."');
      return;
    }

    setIsGenerating(true);
    setGeneratedScenario(null);

    try {
      const result = await createAIScenario({
        category_id: categoryId,
        seq_cnt: questionCount,
        option_cnt: exampleCount,
        prompt: description,
      });

      console.log('✅ AI 시나리오 생성 성공:', result);
      setGeneratedScenario(result);
      alert(`시나리오 "${result.title}"가 생성되었습니다!`);

      // 콜백이 있으면 호출
      onGenerate?.(categoryId, questionCount, exampleCount, description);
    } catch (error) {
      console.error('❌ AI 시나리오 생성 실패:', error);

      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as {
          response?: { status: number; data: { message?: string } };
        };

        const status = axiosError.response?.status;
        const errorMessage = axiosError.response?.data?.message || '알 수 없는 오류가 발생했습니다.';

        // 500 에러: 서버 내부 오류
        if (status === 500) {
          alert(
            `⚠️ 서버 내부 오류 발생\n\n` +
            `${errorMessage}\n\n` +
            `가능한 원인:\n` +
            `• AI 서비스 일시적 장애\n` +
            `• 요청 처리 중 오류\n\n` +
            `잠시 후 다시 시도해주세요.\n` +
            `문제가 지속되면 관리자에게 문의하세요.`
          );
        }
        // 422 에러: 입력 검증 실패
        else if (status === 422) {
          alert(`❌ 입력값 검증 실패\n\n${errorMessage}\n\n시나리오 설명을 더 구체적으로 작성해주세요.`);
        }
        // 기타 에러
        else {
          alert(`시나리오 생성 실패 (${status || 'Unknown'})\n\n${errorMessage}`);
        }
      } else {
        alert('시나리오 생성에 실패했습니다.\n네트워크 연결을 확인해주세요.');
      }
    } finally {
      setIsGenerating(false);
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
              placeholder="시나리오를 구체적으로 설명해주세요 (최소 10자)

              예시:
              • 영화관에서 팝콘을 주문하는 상황을 연습하고 싶어요. 인사하기, 메뉴 고르기, 결제하기를 포함해주세요.
              • 카페에서 음료 주문하기 - 초등 고학년 대상, 정중한 말투 연습
              • 버스 타고 목적지까지 가기 - 요금 지불, 하차벨 누르기, 인사"
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
            disabled={!description.trim() || categoryId === null || isGenerating}
          >
            {isGenerating ? 'AI가 시나리오를 생성하는 중...' : 'AI 시나리오 생성'}
          </button>
        </div>

        {/* 생성된 시나리오 미리보기 */}
        {generatedScenario && (
          <div className="generated-scenario-preview">
            <h3 className="preview-title">✨ 생성된 시나리오</h3>
            <div className="preview-content">
              <div className="preview-item">
                <strong>제목:</strong> {generatedScenario.title}
              </div>
              <div className="preview-item">
                <strong>설명:</strong> {generatedScenario.summary}
              </div>
              <div className="preview-item">
                <strong>질문 개수:</strong> {generatedScenario.sequences.length}개
              </div>
              <div className="preview-note">
                ℹ️ 콘솔(F12)에서 전체 데이터를 확인할 수 있습니다.
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ScenarioGenerator;
