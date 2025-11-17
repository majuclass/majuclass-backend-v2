/** @format */

import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import '../styles/DashBoardPage.css';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
// import type { TooltipItem } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import {
  getCategoryStats,
  getMonthlySessions,
  getSessionSequenceStats,
  getAudioAnswers,
} from '../apis/dashboardApi';
import type {
  CategoryStatsResponse,
  SessionsResponse,
  SessionSequenceStatsResponse,
  SequenceAudioAnswersDto,
  // SequenceStatsDto,
} from '../types/Dashboard';

ChartJS.register(ArcElement, Tooltip, Legend);

const StudentDashboard: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const studentId = Number(id);

  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());

  // API 데이터 상태
  const [categoryStats, setCategoryStats] =
    useState<CategoryStatsResponse | null>(null);
  const [sessions, setSessions] = useState<SessionsResponse | null>(null);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [selectedSession, setSelectedSession] =
    useState<SessionSequenceStatsResponse | null>(null);
  const [showSequenceStats, setShowSequenceStats] = useState(false);

  // 음성 답변 데이터 (sessionId -> sequenceNumber -> 음성 데이터)
  const [audioAnswersMap, setAudioAnswersMap] = useState<
    Record<number, Record<number, SequenceAudioAnswersDto>>
  >({});

  // 전체 세션 중 음성 데이터가 하나라도 있는지 여부
  const [hasAnyAudioData, setHasAnyAudioData] = useState(false);

  // 음성 답변 모달 상태
  const [showAudioModal, setShowAudioModal] = useState(false);
  const [selectedAudioAnswers, setSelectedAudioAnswers] =
    useState<SequenceAudioAnswersDto | null>(null);

  // 카테고리별 통계 로드
  useEffect(() => {
    const loadCategoryStats = async () => {
      try {
        const data = await getCategoryStats(
          studentId,
          currentYear,
          currentMonth
        );
        setCategoryStats(data);
      } catch (error) {
        console.error('카테고리 통계 로드 실패:', error);
        if (error && typeof error === 'object' && 'response' in error) {
          const axiosError = error as { response?: { data: unknown } };
          console.error('서버 응답:', axiosError.response?.data);
        }
      }
    };

    loadCategoryStats();
  }, [studentId, currentYear, currentMonth]);

  // 월별 세션 목록 로드
  useEffect(() => {
    const loadSessions = async () => {
      setSessionsLoading(true);
      try {
        const data = await getMonthlySessions(
          studentId,
          currentYear,
          currentMonth
        );
        setSessions(data);
      } catch (error) {
        console.error('세션 목록 로드 실패:', error);
        if (error && typeof error === 'object' && 'response' in error) {
          const axiosError = error as { response?: { data: unknown } };
          console.error('서버 응답:', axiosError.response?.data);
        }
      } finally {
        setSessionsLoading(false);
      }
    };

    loadSessions();
  }, [studentId, currentYear, currentMonth]);

  // 세션 목록 로드 후 음성 답변 데이터 프리페칭
  useEffect(() => {
    if (!sessions || sessions.sessions.length === 0) {
      setHasAnyAudioData(false);
      setAudioAnswersMap({});
      return;
    }

    const prefetchAudioData = async () => {
      console.log('🎤 음성 답변 데이터 프리페칭 시작...');
      const newAudioMap: Record<number, Record<number, SequenceAudioAnswersDto>> = {};
      let hasAudio = false;

      // 최근 세션부터 순차적으로 로드
      for (const session of sessions.sessions) {
        try {
          const audioData = await getAudioAnswers(session.sessionId);

          // 음성 답변이 있는 시퀀스만 Map에 추가
          const sessionAudioMap: Record<number, SequenceAudioAnswersDto> = {};
          audioData.sequences.forEach((seqAudio: SequenceAudioAnswersDto) => {
            if (
              Array.isArray(seqAudio.audioAnswers) &&
              seqAudio.audioAnswers.length > 0
            ) {
              sessionAudioMap[seqAudio.sequenceNumber] = seqAudio;
              hasAudio = true;
            }
          });

          if (Object.keys(sessionAudioMap).length > 0) {
            newAudioMap[session.sessionId] = sessionAudioMap;
            console.log(`✅ 세션 ${session.sessionId} 음성 데이터 로드 완료`);
          }
        } catch (error) {
          // 음성 답변이 없거나 로드 실패 시 무시하고 계속 진행
          console.log(`ℹ️ 세션 ${session.sessionId} 음성 데이터 없음`);
        }
      }

      setAudioAnswersMap(newAudioMap);
      setHasAnyAudioData(hasAudio);
      console.log(`🎤 프리페칭 완료 - 음성 데이터 있음: ${hasAudio}`);
    };

    prefetchAudioData();
  }, [sessions]);

  // 세션 클릭 핸들러
  const handleSessionClick = async (sessionId: number) => {
    try {
      // 1. 시퀀스 통계 로드
      const data = await getSessionSequenceStats(sessionId);
      setSelectedSession(data);

      // 2. 음성 답변 데이터 확인 (캐싱된 데이터 활용)
      if (audioAnswersMap[sessionId]) {
        // 이미 프리페칭된 데이터가 있으면 재사용
        console.log(`✅ 세션 ${sessionId} 음성 데이터 캐시 사용`);
      } else {
        // 캐시에 없으면 새로 로드 (프리페칭 실패한 경우)
        try {
          const audioData = await getAudioAnswers(sessionId);
          console.log(`✅ 세션 ${sessionId} 음성 답변 로드 성공:`, audioData);

          // 음성 답변이 있는 시퀀스만 Map에 추가
          const sessionAudioMap: Record<number, SequenceAudioAnswersDto> = {};
          audioData.sequences.forEach((seqAudio: SequenceAudioAnswersDto) => {
            if (
              Array.isArray(seqAudio.audioAnswers) &&
              seqAudio.audioAnswers.length > 0
            ) {
              sessionAudioMap[seqAudio.sequenceNumber] = seqAudio;
            }
          });

          if (Object.keys(sessionAudioMap).length > 0) {
            setAudioAnswersMap((prev) => ({
              ...prev,
              [sessionId]: sessionAudioMap,
            }));
            setHasAnyAudioData(true);
          }
        } catch (audioError) {
          console.log(`ℹ️ 세션 ${sessionId} 음성 답변 없음 또는 로드 실패`);
        }
      }

      setShowSequenceStats(true);
    } catch (error) {
      console.error('시퀀스 통계 로드 실패:', error);
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { data: unknown } };
        console.error('서버 응답:', axiosError.response?.data);
      }
    }
  };

  // 월 이동 핸들러
  const handlePrevMonth = () => {
    if (currentMonth === 1) {
      setCurrentMonth(12);
      setCurrentYear(currentYear - 1);
    } else {
      setCurrentMonth(currentMonth - 1);
    }
  };

  const handleNextMonth = () => {
    if (currentMonth === 12) {
      setCurrentMonth(1);
      setCurrentYear(currentYear + 1);
    } else {
      setCurrentMonth(currentMonth + 1);
    }
  };

  // 차트 데이터
  const getChartData = () => {
    if (!categoryStats || categoryStats.categoryStats.length === 0) {
      return null;
    }

    const labels = categoryStats.categoryStats.map((cat) => cat.categoryName);
    const data = categoryStats.categoryStats.map((cat) => cat.sessionCount);

    // 색상 팔레트
    const colors = [
      'rgba(99, 179, 237, 0.6)',
      'rgba(255, 206, 86, 0.6)',
      'rgba(75, 192, 192, 0.6)',
      'rgba(255, 159, 64, 0.6)',
      'rgba(153, 102, 255, 0.6)',
      'rgba(255, 99, 132, 0.6)',
    ];

    const borderColors = [
      'rgba(99, 179, 237, 1)',
      'rgba(255, 206, 86, 1)',
      'rgba(75, 192, 192, 1)',
      'rgba(255, 159, 64, 1)',
      'rgba(153, 102, 255, 1)',
      'rgba(255, 99, 132, 1)',
    ];

    return {
      labels,
      datasets: [
        {
          data,
          backgroundColor: colors.slice(0, data.length),
          borderColor: borderColors.slice(0, data.length),
          borderWidth: 2,
        },
      ],
    };
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 20,
          font: {
            size: 14,
            family: "'Pretendard', sans-serif",
          },
        },
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: {
          size: 14,
        },
        bodyFont: {
          size: 13,
        },
        callbacks: {
          // label: function (context: TooltipItem<'doughnut'>) {
          label: function (context: { label?: string; parsed?: number }) {
            const label = context.label || '';
            const value = context.parsed || 0;
            const total = categoryStats?.totalSessions || 0;
            const percentage =
              total > 0 ? ((value / total) * 100).toFixed(1) : 0;
            return `${label}: ${value}회 (${percentage}%)`;
          },
        },
      },
    },
  };

  // 세션 상태 배지 렌더링
  const renderStatusBadge = (status: string) => {
    const statusMap = {
      COMPLETED: { text: '완료', className: 'status-completed' },
      IN_PROGRESS: { text: '중단', className: 'status-progress' }, // 현재는 진행 중인 시나리오를 연결해서 할 수 없으므로
      ABORTED: { text: '중단', className: 'status-aborted' },
    };

    const statusInfo = statusMap[status as keyof typeof statusMap] || {
      text: status,
      className: '',
    };

    return (
      <span className={`status-badge ${statusInfo.className}`}>
        {statusInfo.text}
      </span>
    );
  };

  // 정답률에 따른 색상 클래스
  const getAccuracyClass = (accuracy: number) => {
    if (accuracy >= 80) return 'accuracy-high';
    if (accuracy >= 50) return 'accuracy-medium';
    return 'accuracy-low';
  };

  // 음성 답변 보기 핸들러
  const handleAudioAnswersClick = (sessionId: number, sequenceNumber: number) => {
    const sessionAudioData = audioAnswersMap[sessionId];
    if (sessionAudioData) {
      const audioData = sessionAudioData[sequenceNumber];
      if (audioData) {
        setSelectedAudioAnswers(audioData);
        setShowAudioModal(true);
      }
    }
  };

  return (
    <div className="student-dashboard">

      <div className="dashboard-content">
        {/* 좌측: 카테고리별 차트 */}
        <div className="dashboard-left">
          <div className="detail-card chart-card">
            <div className="card-header">
              <h3 className="card-title">카테고리별 활동</h3>
              <div className="month-selector">
                <button className="month-nav-btn" onClick={handlePrevMonth}>
                  ←
                </button>
                <span className="month-display">
                  {currentYear}년 {currentMonth}월
                </span>
                <button className="month-nav-btn" onClick={handleNextMonth}>
                  →
                </button>
              </div>
            </div>
            <div className="chart-container">
              {categoryStats && getChartData() ? (
                <>
                  <Doughnut data={getChartData()!} options={chartOptions} />
                  <div className="total-sessions">
                    총 세션: {categoryStats.totalSessions}회
                  </div>
                </>
              ) : (
                <div className="no-data">이번 달 활동 데이터가 없습니다.</div>
              )}
            </div>
          </div>
        </div>

        {/* 우측: 세션 리스트 */}
        <div className="dashboard-right">
          <div className="detail-card sessions-card">
            <h3 className="card-title">최근 시나리오 활동</h3>
            <div className="sessions-list">
              {sessionsLoading ? (
                <div className="loading-data">로딩 중...</div>
              ) : sessions && sessions.sessions.length > 0 ? (
                sessions.sessions.map((session) => (
                  <div
                    key={session.sessionId}
                    className="session-item"
                    onClick={() => handleSessionClick(session.sessionId)}
                  >
                    <div className="session-thumbnail">
                      {session.thumbnailUrl ? (
                        <img
                          src={session.thumbnailUrl}
                          alt={session.scenarioTitle}
                        />
                      ) : (
                        <div className="thumbnail-placeholder">이미지 없음</div>
                      )}
                    </div>
                    <div className="session-info">
                      <div className="session-title">
                        {session.scenarioTitle}
                      </div>
                      <div className="session-meta">
                        <span className="session-category">
                          {session.categoryName}
                        </span>
                        <span className="session-date">
                          {new Date(session.createdAt).toLocaleDateString(
                            'ko-KR'
                          )}
                        </span>
                      </div>
                    </div>
                    <div className="session-status">
                    {renderStatusBadge(session.status)}
                    </div>
                  </div>
                ))
              ) : (
                <div className="no-data">활동 기록이 없습니다.</div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 시퀀스별 정답률 모달 */}
      {showSequenceStats && selectedSession && (
        <div
          className="modal-overlay"
          onClick={() => setShowSequenceStats(false)}
        >
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selectedSession.scenarioTitle} - 상세 결과</h2>
              <button
                className="modal-close"
                onClick={() => setShowSequenceStats(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="session-summary">
                <div className="summary-item">
                  <span className="summary-label">전체 시퀀스:</span>
                  <span className="summary-value">
                    {selectedSession.totalSequences}개
                  </span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">완료 시퀀스:</span>
                  <span className="summary-value">
                    {selectedSession.completedSequences}개
                  </span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">평균 정답률:</span>
                  <span
                    className={`summary-value ${getAccuracyClass(
                      selectedSession.averageAccuracy
                    )}`}
                  >
                    {selectedSession.averageAccuracy.toFixed(1)}%
                  </span>
                </div>
              </div>

              <table className="sequence-stats-table">
                <thead>
                  <tr>
                    <th>순서</th>
                    <th>질문</th>
                    <th>시도 횟수</th>
                    <th>정답률</th>
                    <th>결과</th>
                    {hasAnyAudioData && <th>음성 답변</th>}
                  </tr>
                </thead>
                <tbody>
                  {selectedSession.sequenceStats.map((seq) => {
                    const sessionAudioData = audioAnswersMap[selectedSession.sessionId];
                    const sequenceAudioData = sessionAudioData?.[seq.sequenceNumber];

                    return (
                      <tr key={seq.sequenceId}>
                        <td>{seq.sequenceNumber}</td>
                        <td className="question-cell">{seq.question}</td>
                        <td>{seq.successAttempt}회</td>
                        <td>
                          <span
                            className={`accuracy ${getAccuracyClass(
                              seq.accuracyRate
                            )}`}
                          >
                            {seq.accuracyRate.toFixed(1)}%
                          </span>
                        </td>
                        <td>
                          <span
                            className={`result-badge ${
                              seq.isCorrect ? 'correct' : 'incorrect'
                            }`}
                          >
                            {seq.isCorrect ? '정답' : '오답'}
                          </span>
                        </td>
                        {hasAnyAudioData && (
                          <td>
                            {sequenceAudioData ? (
                              <button
                                className="btn-audio"
                                onClick={() =>
                                  handleAudioAnswersClick(
                                    selectedSession.sessionId,
                                    seq.sequenceNumber
                                  )
                                }
                              >
                                🎤 듣기 ({sequenceAudioData.totalAttempts})
                              </button>
                            ) : (
                              <span className="no-audio">-</span>
                            )}
                          </td>
                        )}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* 음성 답변 모달 */}
      {showAudioModal && selectedAudioAnswers && (
        <div
          className="modal-overlay"
          onClick={() => {
            setShowAudioModal(false);
            setSelectedAudioAnswers(null);
          }}
        >
          <div
            className="modal-content audio-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h2>
                🎤 음성 답변 기록 (시퀀스 {selectedAudioAnswers.sequenceNumber})
              </h2>
              <button
                className="modal-close"
                onClick={() => {
                  setShowAudioModal(false);
                  setSelectedAudioAnswers(null);
                }}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="audio-count">
                총 {selectedAudioAnswers.totalAttempts}번 시도
              </p>

              <div className="audio-answers-list">
                {selectedAudioAnswers.audioAnswers.map((audio) => (
                  <div key={audio.answerId} className="audio-answer-item">
                    <div className="audio-answer-header">
                      <span className="attempt-badge">
                        시도 {audio.attemptNo}
                      </span>
                      <span
                        className={`result-badge ${
                          audio.isCorrect ? 'correct' : 'incorrect'
                        }`}
                      >
                        {audio.isCorrect ? '✓ 정답' : '✗ 오답'}
                      </span>
                      <span className="audio-time">
                        {new Date(audio.createdAt).toLocaleString('ko-KR')}
                      </span>
                    </div>
                    <div className="audio-player-wrapper">
                      <audio controls className="audio-player">
                        <source src={audio.audioUrl} type="audio/wav" />
                        <source src={audio.audioUrl} type="audio/mpeg" />
                        브라우저가 오디오를 지원하지 않습니다.
                      </audio>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentDashboard;
