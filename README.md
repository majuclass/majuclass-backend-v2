# 마주교실 (Maju Class)

# [🏫 마주교실 바로가기](https://www.majuclass.com)
### **SSAFY 13기 자율 프로젝트 3등**  

## 📚 프로젝트 소개

발달장애 사회적 상황 시뮬레이션 서비스

## 🎨 미리보기

<img src="./assets/images/scenario_preview1.png" alt="scenario_preview1" width="600px" />
<img src="./assets/images/scenario_preview2.png" alt="scenario_preview2" width="600px" />
<img src="./assets/images/scenario_preview3.png" alt="scenario_preview3" width="600px" />


## 🏗️ ERD
<img src="./assets/images/erd-ver2.png" alt="erd-ver2" width="800px" />

## 📝 서비스 개요
마주교실은 발달장애를 가진 학생들이 일상에서 일어나는 다양한 상황을 안전하게 연습할 수 있는 시뮬레이션 기반 교육 플랫폼입니다. 

### 목표

학생들을 위한 교보재 제작 간편화를 통해 선생님들의 교보재 제작 피로도를 낮추고,  
학생들이 쉽고 재밌게 여러 상황을 학습하게 합니다.

## 🔍 문제 정의

### 1. 선생님마다 교보재 제작의 시간, 퀄리티가 다르다고 한다

### 2. 반복적으로 학습하게 하기 어려움

## 💡 해결 방안

### AI와 함께 시나리오를 만들 수 있는 일관된 템플릿 제공

### 난이도별로 제공되는 시나리오 언제든 반복적으로 학습 가능

## ✨ 주요 기능

### 교사 기능
- **학생 관리**: CRUD, CSV 일괄 등록
- **시나리오 생성**: 
  - 수동 생성 (질문/답변/픽토그램)
  - AI 자동 생성 (RAG 기반, 백그라운드 처리)
- **학습 분석**: 통계 대시보드, 월별 캘린더

### 학생 기능  
- **난이도별 시뮬레이션**:
  - EASY: 이미지 선택
  - NORMAL: 텍스트 선택
  - HARD: 음성 답변 (STS 분석)
- **실시간 피드백**: 정답/오답 즉시 확인
- **음성 지원**: TTS 질문 읽기, STS 답변 분석

## 🛠️ 기술 스택

### Frontend
- **Language**: TypeScript
- **Framework**: React, Vite
- **UI/스타일링**: TailwindCSS, React Icons, Lottie React React-chartjs-2
- **상태관리**: Zustand, React Query(TanStack Query)
- **라우팅**: React Router Dom
- **개발 도구**: ESLint
- **유틸리티**: Axios

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6 
- **Database**: MySQL, Redis
- **ORM**: Spring Data JPA
- **인증/보안**: Spring Security, JWT
- **개발도구**: Lombok, SpringDoc OpenAPI, Logback, Gradle

### AI Service
- **Language**: Python 3.11
- **Framework**: FastAPI 0.115.5, Uvicorn 0.32.1 
- **Database**: MySQL, ChromaDB
- **ORM**: SQLAlchemy
- **인증/보안**: python-jose
- **AI 라이브러리**: PyTorch, sentence-transformers, LangChain, OpenAI, NumPy

### Infrastructure
- **컨테이너**: Docker Compose
- **Cloud**: AWS S3, AWS ApiGatewway, AWS Lambda, AWS Athena
- **CI/CD**: GitLab runner
