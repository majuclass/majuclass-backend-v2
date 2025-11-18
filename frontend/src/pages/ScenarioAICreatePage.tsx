import React, { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import api from '../apis/apiInstance';
import { useAIGenerationStore } from '../stores/useSenarioAICreateStore';
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
  const navigate = useNavigate();
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [questionCount, setQuestionCount] = useState<number>(5);
  const [exampleCount, setExampleCount] = useState<number>(3);
  const [description, setDescription] = useState<string>('');

  // 전역 스토어에서 AI 생성 상태 가져오기
  const {
    isGenerating,
    generatedScenario,
    error: generationError,
    startGeneration,
    clearGeneration,
  } = useAIGenerationStore();

  // 시나리오 실제 생성 상태
  const [isCreating, setIsCreating] = useState(false);

  // 알람 중복 방지 플래그
  const hasShownGenerationAlert = useRef(false);
  const hasShownErrorAlert = useRef(false);

  // 컴포넌트 언마운트 시 에러만 초기화 (생성 결과는 유지)
  useEffect(() => {
    return () => {
      if (generationError) {
        clearGeneration();
      }
    };
  }, [generationError, clearGeneration]);

  // 생성 완료 시 alert 표시 (중복 방지)
  useEffect(() => {
    if (generatedScenario && !isGenerating && !hasShownGenerationAlert.current) {
      alert(`시나리오 "${generatedScenario.title}"가 생성되었습니다!`);
      hasShownGenerationAlert.current = true;
    }
  }, [generatedScenario, isGenerating]);

  // 에러 발생 시 alert 표시 (중복 방지)
  useEffect(() => {
    if (generationError && !hasShownErrorAlert.current) {
      alert(`❌ 시나리오 생성 실패\n\n${generationError}`);
      hasShownErrorAlert.current = true;
    }
  }, [generationError]);

  // 새로운 시나리오 생성 시작 시 플래그 리셋
  useEffect(() => {
    if (isGenerating) {
      hasShownGenerationAlert.current = false;
      hasShownErrorAlert.current = false;
    }
  }, [isGenerating]);

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

    // 전역 스토어에서 백그라운드 생성 시작
    await startGeneration({
      category_id: categoryId,
      seq_cnt: questionCount,
      option_cnt: exampleCount,
      prompt: description,
    });

    // 콜백이 있으면 호출
    onGenerate?.(categoryId, questionCount, exampleCount, description);
  };

  // 시나리오 실제 생성 (DB 저장)
  const handleCreateScenario = async () => {
    if (!generatedScenario) {
      alert('생성된 시나리오가 없습니다.');
      return;
    }

    setIsCreating(true);

    try {
      const scenarioData = {
        title: generatedScenario.title,
        summary: generatedScenario.summary,
        categoryId: generatedScenario.categoryId,
        sequences: generatedScenario.sequences,
        ...(generatedScenario.thumbnailS3Key && { thumbnailS3Key: generatedScenario.thumbnailS3Key }),
        ...(generatedScenario.backgroundS3Key && { backgroundS3Key: generatedScenario.backgroundS3Key }),
      };

      console.log('📤 시나리오 생성 요청:', scenarioData);

      const response = await api.post('/scenarios/create', scenarioData);

      console.log('✅ 시나리오 생성 성공:', response.data);

      if (response.data.status === 'SUCCESS') {
        alert(`시나리오 "${generatedScenario.title}"가 성공적으로 생성되었습니다!`);

        // 시나리오 목록 페이지로 이동
        navigate('/scenarios');
      }
    } catch (error) {
      console.error('❌ 시나리오 생성 실패:', error);

      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as {
          response?: { status: number; data: { message?: string } };
        };

        const status = axiosError.response?.status;
        const errorMessage = axiosError.response?.data?.message || '알 수 없는 오류가 발생했습니다.';

        alert(`시나리오 생성 실패 (${status || 'Unknown'})\n\n${errorMessage}`);
      } else {
        alert('시나리오 생성에 실패했습니다.\n네트워크 연결을 확인해주세요.');
      }
    } finally {
      setIsCreating(false);
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
                <strong>카테고리:</strong>{' '}
                {categoryList.find((cat) => cat.id === generatedScenario.categoryId)?.categoryName || '알 수 없음'}
              </div>
              <div className="preview-item">
                <strong>질문 개수:</strong> {generatedScenario.sequences.length}개
              </div>

              {/* 시퀀스별 상세 정보 */}
              <div className="sequences-detail">
                <h4 className="sequences-title">📝 질문 및 선택지</h4>
                {generatedScenario.sequences.map((seq) => (
                  <div key={seq.seqNo} className="sequence-item">
                    <div className="sequence-header">
                      <strong>질문 {seq.seqNo}:</strong> {seq.question}
                    </div>
                    <div className="options-list">
                      {seq.options.map((opt) => (
                        <div
                          key={opt.optionNo}
                          className={`option-item ${opt.isAnswer ? 'correct-answer' : ''}`}
                        >
                          <span className="option-number">{opt.optionNo}.</span>
                          <span className="option-text">{opt.optionText}</span>
                          {opt.isAnswer && (
                            <span className="answer-badge">✓ 정답</span>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              {/* 시나리오 생성 버튼 */}
              <div className="create-button-wrapper">
                <button
                  className="create-scenario-button"
                  onClick={handleCreateScenario}
                  disabled={isCreating}
                >
                  {isCreating ? '시나리오 생성 중...' : '✅ 시나리오 생성하기'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ScenarioGenerator;
