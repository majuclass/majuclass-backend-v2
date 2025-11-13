/** @format */

import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import NavBar from '../components/NavBar';
import '../styles/DashBoardPage.css';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, } from 'chart.js';
// import type { TooltipItem } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import {
  getCategoryStats,
  getMonthlySessions,
  getSessionSequenceStats,
} from '../apis/dashboardApi';
import type {
  CategoryStatsResponse,
  SessionsResponse,
  SessionSequenceStatsResponse,
  // SessionListItemDto,
} from '../types/Dashboard';

ChartJS.register(ArcElement, Tooltip, Legend);

const StudentDashboard: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const studentId = Number(id);

  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());

  // API 데이터 상태
  const [categoryStats, setCategoryStats] = useState<CategoryStatsResponse | null>(null);
  const [sessions, setSessions] = useState<SessionsResponse | null>(null);
  const [selectedSession, setSelectedSession] = useState<SessionSequenceStatsResponse | null>(null);
  const [showSequenceStats, setShowSequenceStats] = useState(false);

  // 카테고리별 통계 로드
  useEffect(() => {
    const loadCategoryStats = async () => {
      try {
        const data = await getCategoryStats(studentId, currentYear, currentMonth);
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
      try {
        const data = await getMonthlySessions(studentId, currentYear, currentMonth);
        setSessions(data);
      } catch (error) {
        console.error('세션 목록 로드 실패:', error);
        if (error && typeof error === 'object' && 'response' in error) {
          const axiosError = error as { response?: { data: unknown } };
          console.error('서버 응답:', axiosError.response?.data);
        }
      }
    };

    loadSessions();
  }, [studentId, currentYear, currentMonth]);

  // 세션 클릭 핸들러
  const handleSessionClick = async (sessionId: number) => {
    try {
      const data = await getSessionSequenceStats(sessionId);
      setSelectedSession(data);
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
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
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
      IN_PROGRESS: { text: '진행중', className: 'status-progress' },
      ABORTED: { text: '중단', className: 'status-aborted' },
    };

    const statusInfo = statusMap[status as keyof typeof statusMap] || {
      text: status,
      className: '',
    };

    return <span className={`status-badge ${statusInfo.className}`}>{statusInfo.text}</span>;
  };

  // 정답률에 따른 색상 클래스
  const getAccuracyClass = (accuracy: number) => {
    if (accuracy >= 80) return 'accuracy-high';
    if (accuracy >= 50) return 'accuracy-medium';
    return 'accuracy-low';
  };

  return (
    <div className="student-dashboard">
      <NavBar />

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
              {sessions && sessions.sessions.length > 0 ? (
                sessions.sessions.map((session) => (
                  <div
                    key={session.sessionId}
                    className="session-item"
                    onClick={() => handleSessionClick(session.sessionId)}
                  >
                    <div className="session-thumbnail">
                      {session.thumbnailUrl ? (
                        <img src={session.thumbnailUrl} alt={session.scenarioTitle} />
                      ) : (
                        <div className="thumbnail-placeholder">이미지 없음</div>
                      )}
                    </div>
                    <div className="session-info">
                      <div className="session-title">{session.scenarioTitle}</div>
                      <div className="session-meta">
                        <span className="session-category">{session.categoryName}</span>
                        <span className="session-date">
                          {new Date(session.createdAt).toLocaleDateString('ko-KR')}
                        </span>
                      </div>
                    </div>
                    {renderStatusBadge(session.status)}
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
        <div className="modal-overlay" onClick={() => setShowSequenceStats(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selectedSession.scenarioTitle} - 상세 결과</h2>
              <button className="modal-close" onClick={() => setShowSequenceStats(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="session-summary">
                <div className="summary-item">
                  <span className="summary-label">전체 시퀀스:</span>
                  <span className="summary-value">{selectedSession.totalSequences}개</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">완료 시퀀스:</span>
                  <span className="summary-value">{selectedSession.completedSequences}개</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">평균 정답률:</span>
                  <span className={`summary-value ${getAccuracyClass(selectedSession.averageAccuracy)}`}>
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
                  </tr>
                </thead>
                <tbody>
                  {selectedSession.sequenceStats.map((seq) => (
                    <tr key={seq.sequenceId}>
                      <td>{seq.sequenceNumber}</td>
                      <td className="question-cell">{seq.question}</td>
                      <td>{seq.successAttempt}회</td>
                      <td>
                        <span className={`accuracy ${getAccuracyClass(seq.accuracyRate)}`}>
                          {seq.accuracyRate.toFixed(1)}%
                        </span>
                      </td>
                      <td>
                        <span className={`result-badge ${seq.isCorrect ? 'correct' : 'incorrect'}`}>
                          {seq.isCorrect ? '정답' : '오답'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentDashboard;
