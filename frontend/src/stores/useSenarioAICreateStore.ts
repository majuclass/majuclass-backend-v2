import { create } from 'zustand';
import { createAIScenario } from '../apis/scenarioAiApi';
import type { AIScenarioCreateResponse } from '../apis/scenarioAiApi';

interface AIGenerationState {
  // 상태
  isGenerating: boolean;
  generatedScenario: AIScenarioCreateResponse | null;
  error: string | null;
  showNotification: boolean; // NavBar 알림 표시 여부

  // 액션
  startGeneration: (params: {
    category_id: number;
    seq_cnt: number;
    option_cnt: number;
    prompt: string;
  }) => Promise<void>;

  clearGeneration: () => void;
  hideNotification: () => void; // 알림만 숨기기 (데이터는 유지)
}

export const useAIGenerationStore = create<AIGenerationState>((set) => ({
  // 초기 상태
  isGenerating: false,
  generatedScenario: null,
  error: null,
  showNotification: false,

  // AI 시나리오 생성 시작
  startGeneration: async (params) => {
    set({
      isGenerating: true,
      error: null,
      generatedScenario: null,
      showNotification: false,
    });

    console.log('🚀 [백그라운드] AI 시나리오 생성 시작...', params);

    try {
      const result = await createAIScenario(params);

      console.log('✅ [백그라운드] AI 시나리오 생성 완료:', result.title);

      set({
        generatedScenario: result,
        isGenerating: false,
        error: null,
        showNotification: true, // 생성 완료 시 알림 표시
      });

      // 브라우저 알림 (선택적 - 권한이 있을 때만)
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification('AI 시나리오 생성 완료!', {
          body: `"${result.title}"가 생성되었습니다.`,
          icon: '/logo_text.png',
        });
      }

    } catch (error) {
      console.error('❌ [백그라운드] AI 시나리오 생성 실패:', error);

      let errorMessage = '알 수 없는 오류가 발생했습니다.';

      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as {
          response?: { status: number; data: { message?: string } };
        };

        const status = axiosError.response?.status;
        const backendMessage = axiosError.response?.data?.message;

        if (status === 500) {
          errorMessage = `서버 오류: ${backendMessage || 'AI 서비스 일시적 장애'}`;
        } else if (status === 422) {
          errorMessage = `입력값 오류: ${backendMessage || '시나리오 설명을 더 구체적으로 작성해주세요.'}`;
        } else {
          errorMessage = backendMessage || `오류 발생 (${status})`;
        }
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }

      set({
        error: errorMessage,
        isGenerating: false,
      });
    }
  },

  // 알림만 숨기기 (데이터는 유지)
  hideNotification: () => {
    set({ showNotification: false });
  },

  // 생성 상태 초기화 (모든 데이터 삭제)
  clearGeneration: () => {
    set({
      isGenerating: false,
      generatedScenario: null,
      error: null,
      showNotification: false,
    });
  },
}));
