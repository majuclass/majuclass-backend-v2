/** @format */

import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
// import { AxiosError } from "axios";
import NavBar from "../components/NavBar";
import "../styles/MainPage.css";
import {
  getStudents,
  createStudent,
  updateStudent,
  deleteStudent,
  getMonthlyCalendar,
  getDailySessions,
} from "../apis/mainApi";
import type {
  StudentResponse,
  CalendarMonthlyResponse,
  DailySessionListResponse,
} from "../types/MainPage";

const MainPage: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 학생 관련 상태
  const [students, setStudents] = useState<StudentResponse[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<StudentResponse | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [newStudentName, setNewStudentName] = useState("");
  const [editStudentName, setEditStudentName] = useState("");

  // 달력 관련 상태
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [calendarData, setCalendarData] = useState<CalendarMonthlyResponse | null>(null);

  // 일일 세션 모달 상태
  const [showDailySessionsModal, setShowDailySessionsModal] = useState(false);
  const [dailySessions, setDailySessions] = useState<DailySessionListResponse | null>(null);

  // 학생 목록 로드
  useEffect(() => {
    loadStudents();
  }, []);

  // 달력 데이터 로드
  useEffect(() => {
    loadCalendarData();
  }, [currentYear, currentMonth]);

  const loadStudents = async () => {
    try {
      const data = await getStudents();
      setStudents(data);
    } catch (error) {
      console.error("학생 목록 로드 실패:", error);
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { data: unknown } };
        console.error("서버 응답:", axiosError.response?.data);
      }
    }
  };

  const loadCalendarData = async () => {
    try {
      const data = await getMonthlyCalendar(currentYear, currentMonth);
      setCalendarData(data);
    } catch (error) {
      console.error("달력 데이터 로드 실패:", error);

      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as {
          response?: { status: number; data: unknown };
          config?: { url?: string; params?: unknown };
        };
        console.error("상태 코드:", axiosError.response?.status);
        console.error("서버 응답:", axiosError.response?.data);
        console.error("요청 URL:", axiosError.config?.url);
        console.error("요청 파라미터:", axiosError.config?.params);
      }

      // 임시로 빈 데이터 설정하여 UI가 깨지지 않도록 처리
      setCalendarData({
        year: currentYear,
        month: currentMonth,
        dailyStats: [],
        totalDays: 0
      });
    }
  };

  // 학생 추가
  const handleAddStudent = async () => {
    if (!newStudentName.trim()) {
      alert("학생 이름을 입력해주세요.");
      return;
    }

    try {
      await createStudent(newStudentName);
      setNewStudentName("");
      setShowAddModal(false);
      loadStudents();
    } catch (error) {
      console.error("학생 추가 실패:", error);
      alert("학생 추가에 실패했습니다.");
    }
  };

  // 학생 수정
  const handleEditStudent = async () => {
    if (!selectedStudent) return;
    if (!editStudentName.trim()) {
      alert("학생 이름을 입력해주세요.");
      return;
    }

    try {
      await updateStudent(selectedStudent.studentId, { name: editStudentName });
      setEditStudentName("");
      setShowEditModal(false);
      setSelectedStudent(null);
      loadStudents();
    } catch (error) {
      console.error("학생 수정 실패:", error);
      alert("학생 수정에 실패했습니다.");
    }
  };

  // 학생 삭제
  const handleDeleteStudent = async (studentId: number, studentName: string) => {
    if (!confirm(`"${studentName}" 학생을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      await deleteStudent(studentId);
      loadStudents();
      if (selectedStudent?.studentId === studentId) {
        setSelectedStudent(null);
      }
    } catch (error) {
      console.error("학생 삭제 실패:", error);
      alert("학생 삭제에 실패했습니다.");
    }
  };

  // CSV 파일 업로드
  const handleCSVUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // ========== CSV 파싱 로직 시작 ==========
    const reader = new FileReader();
    reader.onload = async (e) => {
      const text = e.target?.result as string;
      const lines = text.split("\n").filter((line) => line.trim());

      // 첫 줄은 헤더로 가정 (name 등)
      const headers = lines[0].split(",").map((h) => h.trim());
      const nameIndex = headers.findIndex(
        (h) => h.toLowerCase() === "name" || h === "이름"
      );

      if (nameIndex === -1) {
        alert('CSV 파일에 "name" 또는 "이름" 열이 필요합니다.');
        return;
      }

      // 데이터 행 파싱
      const studentNames: string[] = [];
      for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(",").map((v) => v.trim());
        const name = values[nameIndex];
        if (name) {
          studentNames.push(name);
        }
      }

      // API 호출하여 학생 일괄 추가
      try {
        for (const name of studentNames) {
          await createStudent(name);
        }
        alert(`${studentNames.length}명의 학생이 추가되었습니다.`);
        loadStudents();
      } catch (error) {
        console.error("CSV 학생 추가 실패:", error);
        alert("일부 학생 추가에 실패했습니다.");
      }
    };
    reader.readAsText(file);
    // ========== CSV 파싱 로직 끝 ==========

    // 파일 입력 초기화
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  // 월 이동
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

  // 달력 일자 클릭 (일일 세션 조회)
  const handleDayClick = async (
    studentId: number,
    // studentName: string,
    day: number
  ) => {
    const dateStr = `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(day).padStart(2, "0")}`;

    try {
      const data = await getDailySessions(studentId, dateStr);
      setDailySessions(data);
      setShowDailySessionsModal(true);
    } catch (error) {
      console.error("일일 세션 로드 실패:", error);
      alert("일일 세션 조회에 실패했습니다.");
    }
  };

  // 달력 렌더링
  const renderCalendar = () => {
    const firstDay = new Date(currentYear, currentMonth - 1, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();

    const days = [];
    const weekDays = ["일", "월", "화", "수", "목", "금", "토"];

    // 요일 헤더
    weekDays.forEach((day) => {
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
      const dayData = calendarData?.dailyStats.find((d) => new Date(d.date).getDate() === day);
      const isToday =
        new Date().getDate() === day &&
        new Date().getMonth() + 1 === currentMonth &&
        new Date().getFullYear() === currentYear;

      days.push(
        <div
          key={`day-${day}`}
          className={`calendar-day ${isToday ? "today" : ""}`}
        >
          <div className="day-number">{day}</div>
          {dayData && dayData.studentSessions.length > 0 && (
            <div className="day-activities">
              {dayData.studentSessions.map((activity) => (
                <div
                  key={activity.studentId}
                  className="activity-item"
                  onClick={() =>
                    handleDayClick(activity.studentId, day)
                  }
                >
                  <span className="activity-student">{activity.studentName}</span>
                  <span className="activity-count">{activity.sessionCount}회</span>
                </div>
              ))}
            </div>
          )}
        </div>
      );
    }

    return days;
  };

  // 상태 배지 렌더링
  const renderStatusBadge = (status: string) => {
    const statusMap = {
      COMPLETED: { text: "완료", className: "status-completed" },
      IN_PROGRESS: { text: "진행중", className: "status-progress" },
      ABORTED: { text: "중단", className: "status-aborted" },
    };

    const statusInfo = statusMap[status as keyof typeof statusMap] || {
      text: status,
      className: "",
    };

    return (
      <span className={`status-badge ${statusInfo.className}`}>
        {statusInfo.text}
      </span>
    );
  };

  return (
    <div className="main-page">
      <NavBar />

      <div className="main-content">
        {/* 왼쪽: 학생 목록 */}
        <div className="students-section">
          <div className="section-header">
            <h2>학생 목록</h2>
            <div className="action-buttons">
              <button
                className="btn-add"
                onClick={() => setShowAddModal(true)}
              >
                + 추가
              </button>
              <button
                className="btn-csv"
                onClick={() => fileInputRef.current?.click()}
              >
                📁 CSV 업로드
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                style={{ display: "none" }}
                onChange={handleCSVUpload}
              />
            </div>
          </div>

          <div className="students-list">
            {students.length === 0 ? (
              <div className="no-data">학생이 없습니다.</div>
            ) : (
              students.map((student) => (
                <div
                  key={student.studentId}
                  className={`student-item ${selectedStudent?.studentId === student.studentId ? "active" : ""}`}
                  onClick={() => setSelectedStudent(student)}
                >
                  <div className="student-info">
                    <span className="student-name">{student.name}</span>
                    <span className="student-school">{student.schoolName}</span>
                  </div>
                  <div className="student-actions">
                    <button
                      className="btn-view"
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/students/${student.studentId}`);
                      }}
                    >
                      보기
                    </button>
                    <button
                      className="btn-edit"
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelectedStudent(student);
                        setEditStudentName(student.name);
                        setShowEditModal(true);
                      }}
                    >
                      수정
                    </button>
                    <button
                      className="btn-delete"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteStudent(student.studentId, student.name);
                      }}
                    >
                      삭제
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* 오른쪽: 달력 */}
        <div className="calendar-section">
          <div className="calendar-header">
            <button className="month-nav-btn" onClick={handlePrevMonth}>
              ←
            </button>
            <h2 className="calendar-title">
              {currentYear}년 {currentMonth}월
            </h2>
            <button className="month-nav-btn" onClick={handleNextMonth}>
              →
            </button>
          </div>

          <div className="calendar-grid">{renderCalendar()}</div>
        </div>
      </div>

      {/* 학생 추가 모달 */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>학생 추가</h2>
              <button
                className="modal-close"
                onClick={() => setShowAddModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>학생 이름</label>
                <input
                  type="text"
                  value={newStudentName}
                  onChange={(e) => setNewStudentName(e.target.value)}
                  placeholder="이름을 입력하세요"
                  onKeyPress={(e) => e.key === "Enter" && handleAddStudent()}
                />
              </div>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setShowAddModal(false)}>
                  취소
                </button>
                <button className="btn-confirm" onClick={handleAddStudent}>
                  추가
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 학생 수정 모달 */}
      {showEditModal && selectedStudent && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>학생 수정</h2>
              <button
                className="modal-close"
                onClick={() => setShowEditModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>학생 이름</label>
                <input
                  type="text"
                  value={editStudentName}
                  onChange={(e) => setEditStudentName(e.target.value)}
                  placeholder="이름을 입력하세요"
                  onKeyPress={(e) => e.key === "Enter" && handleEditStudent()}
                />
              </div>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setShowEditModal(false)}>
                  취소
                </button>
                <button className="btn-confirm" onClick={handleEditStudent}>
                  수정
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 일일 세션 모달 */}
      {showDailySessionsModal && dailySessions && (
        <div
          className="modal-overlay"
          onClick={() => setShowDailySessionsModal(false)}
        >
          <div className="modal-content large" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>
                {dailySessions.studentName} - {dailySessions.date} 활동
              </h2>
              <button
                className="modal-close"
                onClick={() => setShowDailySessionsModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="session-count">
                총 {dailySessions.totalCount}개의 세션
              </p>
              <div className="sessions-list">
                {dailySessions.sessions.map((session) => (
                  <div key={session.sessionId} className="session-item-modal">
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
                        <span className="session-category">
                          {session.categoryName}
                        </span>
                        <span className="session-time">
                          {new Date(session.createdAt).toLocaleTimeString("ko-KR")}
                          {session.completedAt &&
                            ` - ${new Date(session.completedAt).toLocaleTimeString("ko-KR")}`}
                        </span>
                      </div>
                    </div>
                    {renderStatusBadge(session.status)}
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

export default MainPage;
