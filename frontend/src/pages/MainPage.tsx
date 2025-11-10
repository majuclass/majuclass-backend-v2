//src/pages/StudentsPage.tsx
import React, { useState, } from 'react';
import { useNavigate } from 'react-router-dom';
import NavBar from '../components/NavBar';
import '../styles/StudentsPage.css';

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
  const navigate = useNavigate();
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

  const handleStudentSelect = (student: Student) => {
    setSelectedStudent(student);
  };

  // const handleViewScenario = (scenarioName: string) => {
  //   console.log(`${selectedStudent?.name}의 ${scenarioName} 보기`);
  //   // 시나리오 상세 페이지로 이동하는 로직 추가
  // };

  return (
    <div className="student-dashboard">
      <NavBar />
      
      <div className="dashboard-content">
        <div className="student-list-section">
          <div className="section-header">
            <h2>학생 목록</h2>
            {/* <span className="student-count">이름/학년 검색</span> */}
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
                <button className="view-button"
                onClick={(e) => {
                  e.stopPropagation()
                  console.log('${students.name}의 상세 페이지로 이동')
                  navigate(`/students/${student.id}`); 
                }}
                >
                보기
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="student-detail-section">
          <div className="detail-card attendance-card">
            <h3>즐찾란</h3>
          </div>

          <div className="detail-card activity-card">
            <h3>최근 활동 (학생 선택 시 해당 학생 최근 활동으로 진입)</h3>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;