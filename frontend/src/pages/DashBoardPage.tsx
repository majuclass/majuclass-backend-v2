import React, { useState,  } from 'react'; // useEffect, useRef
import NavBar from '../components/NavBar';
import '../styles/DashBoardPage.css'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

interface Student {
  id: number;
  name: string;
  grade: string;
  scenarios: {
    name: string;
    difficulty: string;
    status: 'completed' | 'in-progress' | 'not-started';
  }[];
}

const StudentDashboard: React.FC = () => {
  const [students] = useState<Student[]>([
    {
      id: 1,
      name: '김가람',
      grade: '초3',
      scenarios: [
        { name: '시나리오 1', difficulty: '하', status: 'completed' },
        { name: '시나리오 2', difficulty: '중', status: 'completed' },
        { name: '시나리오 3', difficulty: '상', status: 'in-progress' },
      ]
    },
    {
      id: 2,
      name: '박도윤',
      grade: '중1',
      scenarios: [
        { name: '시나리오 1', difficulty: '하', status: 'completed' },
        { name: '시나리오 2', difficulty: '중', status: 'not-started' },
        { name: '시나리오 3', difficulty: '상', status: 'not-started' },
      ]
    },
    {
      id: 3,
      name: '어서준',
      grade: '고1',
      scenarios: [
        { name: '시나리오 1', difficulty: '하', status: 'not-started' },
        { name: '시나리오 2', difficulty: '중', status: 'not-started' },
        { name: '시나리오 3', difficulty: '상', status: 'not-started' },
      ]
    },
  ]);

  const [selectedStudent, setSelectedStudent] = useState<Student | null>(students[0]);
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());

  const handleStudentSelect = (student: Student) => {
    setSelectedStudent(student);
  };

  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear(currentYear - 1);
    } else {
      setCurrentMonth(currentMonth - 1);
    }
  };

  const handleNextMonth = () => {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear(currentYear + 1);
    } else {
      setCurrentMonth(currentMonth + 1);
    }
  };

  // Chart 데이터
  const getChartData = () => {
    if (!selectedStudent) return null;

    const completed = selectedStudent.scenarios.filter(s => s.status === 'completed').length;
    const inProgress = selectedStudent.scenarios.filter(s => s.status === 'in-progress').length;
    const notStarted = selectedStudent.scenarios.filter(s => s.status === 'not-started').length;

    return {
      labels: ['카페', '영화관', '식당'],
      datasets: [
        {
          data: [completed, inProgress, notStarted],
          backgroundColor: [
            'rgba(99, 179, 237, 0.6)',
            'rgba(255, 223, 138, 0.6)',
            'rgba(220, 220, 220, 0.6)',
          ],
          borderColor: [
            'rgba(99, 179, 237, 1)',
            'rgba(255, 223, 138, 1)',
            'rgba(220, 220, 220, 1)',
          ],
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
      },
    },
  };

  // 캘린더 생성
  const renderCalendar = () => {
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const today = new Date();
    
    const days = [];
    const weekDays = ['일', '월', '화', '수', '목', '금', '토'];

    // 요일 헤더
    weekDays.forEach(day => {
      days.push(
        <div key={`header-${day}`} className="calendar-day-header">
          {day}
        </div>
      );
    });

    // 빈 칸
    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="calendar-day empty"></div>);
    }

    // 날짜
    for (let day = 1; day <= daysInMonth; day++) {
      const isToday = 
        today.getDate() === day &&
        today.getMonth() === currentMonth &&
        today.getFullYear() === currentYear;
      
      days.push(
        <div key={`day-${day}`} className={`calendar-day ${isToday ? 'today' : ''}`}>
          <span className="day-number">{day}</span>
        </div>
      );
    }

    return days;
  };

  return (
    <div className="student-dashboard">
      <NavBar />
      
      <div className="dashboard-content">
        <div className="student-list-section">
          <div className="section-header">
            <h2>학생 목록</h2>
            <span className="student-count">이름/학년 검색</span>
          </div>
          
          <div className="student-list">
            {students.map((student) => (
              <div
                key={student.id}
                className={`student-item ${selectedStudent?.id === student.id ? 'active' : ''}`}
                onClick={() => handleStudentSelect(student)}
              >
                <div className="student-info">
                  <span className="student-name">{student.name}</span>
                  <span className="student-grade">{student.grade}</span>
                </div>
                <button className="view-button">보기</button>
              </div>
            ))}
          </div>
        </div>

        <div className="student-detail-section">
          <div className="top-cards">
            <div className="detail-card activity-summary-card">
              <h3 className="card-title">(학생 이름) 활동</h3>
              <div className="chart-container">
                {selectedStudent && getChartData() && (
                  <Doughnut data={getChartData()!} options={chartOptions} />
                )}
              </div>
            </div>

            <div className="detail-card calendar-card">
              <div className="calendar-header">
                <button className="calendar-nav-btn" onClick={handlePrevMonth}>←</button>
                <h3 className="calendar-title">{currentMonth + 1}월</h3>
                <button className="calendar-nav-btn" onClick={handleNextMonth}>→</button>
              </div>
              <div className="calendar-grid">
                {renderCalendar()}
              </div>
            </div>
          </div>

          <div className="detail-card activity-card">
            <h3 className="card-title">
              카테고리 별 해당 학생이 챗한 시뮬레이션 들을 상세보기 눌르면 해당 시나리오 질문과 정답을 텍스트로 볼 수 있음
            </h3>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;