# 마주교실 (Maju Class)

# [🏫 마주교실 바로가기](https://www.majuclass.com)
**SSAFY 13기 자율 프로젝트 | 팀 사이**  

## 📚 프로젝트 소개

**마주교실**은 발달장애·통합학급 학생들이 일상 사회 상황을 안전하게 연습할 수 있는 시뮬레이션 기반 교육 플랫폼입니다. 교사는 카페 주문, 영화표 구매 등 실생활 시나리오를 생성하고, 학생들은 난이도별 시뮬레이션을 통해 사회적 상호작용을 학습합니다.
---

## 🏗️ 시스템 아키텍처

```
마주교실 시스템
├── Frontend (React + TypeScript)
│   └── 포트: 5173 (개발) / 80 (프로덕션)
├── Backend API (Spring Boot)
│   └── 포트: 8080
├── AI Service (FastAPI)
│   └── 포트: 8000
├── Database (MySQL 8.4)
│   └── 포트: 3306
├── Cache (Redis 7)
│   └── 포트: 6379
└── Storage (AWS S3, ChromaDB)
```

---

## 🛠️ 기술 스택

### Frontend
- **Core**: React, TypeScript, Vite
- **상태관리**: Zustand, TanStack Query
- **스타일링**: Tailwind CSS, CSS Modules
- **UI/UX**: Lottie, Chart.js, React Icons
- **통신**: Axios (JWT Bearer 인증)

### Backend
- **Core**: Java 21, Spring Boot 3.5.6
- **보안**: Spring Security, JWT
- **데이터**: JPA/Hibernate, MySQL
- **캐시**: Redis
- **통신**: WebClient

### AI Service
- **Core**: Python 3.11, FastAPI
- **AI/ML**: OpenAI GPT, Whisper (STT), ChromaDB
- **임베딩**: 한국어 특화 모델
- **처리**: LangChain (RAG), SentenceTransformers, PyTorch

### Infrastructure
- **컨테이너**: Docker Compose
- **스토리지**: AWS S3
- **문서화**: Swagger, FastAPI Docs

---

## 🎯 주요 기능

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

---

## 📁 프로젝트 구조

```
프로젝트 루트/
├── frontend/                 # React 애플리케이션
│   ├── src/
│   │   ├── apis/            # API 통신
│   │   ├── components/      # React 컴포넌트
│   │   ├── pages/          # 페이지 컴포넌트
│   │   ├── stores/         # Zustand 스토어
│   │   └── types/          # TypeScript 타입
│   └── public/
│
├── backend/                 # Spring Boot API
│   └── src/main/java/
│       ├── domain/         # 비즈니스 도메인
│       │   ├── auth/       # 인증/토큰
│       │   ├── user/       # 사용자
│       │   ├── student/    # 학생
│       │   ├── scenario/   # 시나리오
│       │   └── scenariosession/  # 세션
│       └── global/         # 공통 기능
│           ├── config/     # 설정
│           ├── security/   # JWT
│           └── s3/        # 파일 업로드
│
└── ai/                     # FastAPI AI 서비스
    └── app/
        ├── domains/
        │   ├── speech_to_text/  # STT + 평가
        │   ├── text_to_speech/  # TTS
        │   └── scenario/        # RAG 생성
        └── common/             # 공통 유틸
```

---

## 🚀 시작하기

### 1. 환경 요구사항

- Node.js 18+
- Java 21
- Python 3.11
- Docker & Docker Compose
- MySQL 8.4
- Redis 7

### 2. Docker Compose로 실행

```bash
# 전체 스택 시작
docker compose up -d

# 빌드 후 시작
docker compose up --build -d

# 로그 확인
docker compose logs -f

# 서비스 중지
docker compose down
```

### 3. 개별 서비스 실행

**Frontend:**
```bash
cd frontend
npm install
npm run dev  # http://localhost:5173
```

**Backend:**
```bash
docker compose up spring
```

**AI Service:**
```bash
cd ai
docker compose up astapi
```

---

## 📱 주요 페이지

| 경로 | 설명 |
|------|------|
| `/` | 로그인/회원가입 |
| `/main` | 교사 대시보드 |
| `/scenarios` | 시나리오 목록 |
| `/scenarios/create` | 수동 시나리오 생성 |
| `/scenarios/ai/create` | AI 시나리오 생성 |
| `/simulation/:id` | 시뮬레이션 실행 |
| `/students/:id` | 학생 통계 |

---

## 🔐 인증 및 보안

- **JWT Bearer Token**: Access Token + Refresh Token
- **토큰 저장**: LocalStorage (Frontend)
- **블랙리스트**: Redis (로그아웃 토큰)
- **자동 갱신**: Refresh Token 메커니즘

---

## 📊 API 문서

- **Backend API**: `http://localhost:8080/api/swagger-ui.html`
- **AI Service API**: `http://localhost:8000/docs`

---

## 🎨 핵심 기능 상세

### AI 시나리오 생성 파이프라인
1. GPT 기반 시나리오 자동 생성
2. RAG (Retrieval-Augmented Generation) 활용한 맥락 기반 생성
3. 백그라운드 처리 및 실시간 알림
4. 벡터 데이터베이스 활용

### STS 음성 유사도 평가 시스템
1. 음성 인식 모델 기반 변환
2. 다차원 평가:
   - 의미적 유사도
   - 음성 매칭 점수
   - 키워드 추출 및 교집합 
3. AI 기반 피드백 생성

### 실시간 캐싱 전략
- Redis 캘린더 캐시
- JWT 토큰 관리
- 스케줄러 기반 자동 갱신

---

## 🔄 개발 가이드

### 브랜치 전략
- **메인 브랜치**: `master`
- **개발 브랜치**: FE-dev, BE-dev, 기능별 브랜치 생성
- **커밋 컨벤션**: Conventional Commits

### 코드 스타일
- **Frontend**: ESLint + Prettier
- **Backend**: Google Java Style Guide
- **Python**: PEP 8

---

## 👥 팀 정보

**팀명**: 사이  
**프로젝트**: SSAFY 13기 자율 프로젝트