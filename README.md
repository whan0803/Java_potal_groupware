# 통합 포털 시스템


---

## 기술 스택

### Frontend

* React
* Vite
* JavaScript
* HTML
* CSS
* npm

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security
* Gradle
* Lombok

### Database

* PostgreSQL
* Supabase


# 프로젝트 구조

```text
프로젝트
│
├── front/                      # React Frontend
│   ├── public/
│   ├── src/
│   │   ├── assets/             # 이미지 및 정적 파일
│   │   ├── components/         # 공통 컴포넌트
│   │   ├── pages/              # 페이지 컴포넌트
│   │   ├── api/                # Backend API 통신
│   │   ├── App.jsx
│   │   └── main.jsx
│   │
│   ├── package.json
│   └── vite.config.js
│
├── backend/                    # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ...
│   │   │   │       ├── controller/     # API 요청 처리
│   │   │   │       ├── service/        # 비즈니스 로직
│   │   │   │       ├── repository/     # Database 접근
│   │   │   │       ├── entity/         # JPA Entity
│   │   │   │       ├── dto/            # Request / Response DTO
│   │   │   │       └── config/         # Security 등 설정
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │
│   ├── build.gradle
│   ├── gradlew
│   └── gradlew.bat
│
└── README.md
```

---

# 실행 방법

## 1. 프로젝트 Clone

```bash
git clone [GitHub Repository URL]
```

프로젝트 폴더로 이동합니다.

```bash
cd [프로젝트 폴더명]
```

---

# Frontend 실행

터미널에서 `front` 폴더로 이동합니다.

```bash
cd front
```

패키지를 설치합니다.

```bash
npm install
```

개발 서버를 실행합니다.

```bash
npm run dev
```

실행 후 터미널에 표시되는 주소로 접속합니다.

일반적으로 다음 주소를 사용합니다.

```text
http://localhost:5173
```

---

# Backend 실행

## IntelliJ IDEA에서 실행

1. IntelliJ IDEA 실행
2. `backend` 폴더 열기
3. Gradle Dependency 로딩
4. `BackendApplication` 파일 실행

```text
BackendApplication
└── Run BackendApplication
```

---

## VS Code / Terminal에서 실행

프로젝트의 `backend` 폴더로 이동합니다.

```bash
cd backend
```

Spring Boot를 실행합니다.

### macOS / Linux

```bash
./gradlew bootRun
```

### Windows

```bash
gradlew.bat bootRun
```

기본 Backend 주소

```text
http://localhost:8080
```

---

# Database

Database는 **PostgreSQL 기반 Supabase**를 사용합니다.

Backend의 다음 파일에서 Database 연결 정보를 설정합니다.

```text
backend/src/main/resources/application.properties
```

예시:

```properties
spring.datasource.url=jdbc:postgresql://DB_HOST:5432/postgres
spring.datasource.username=DB_USERNAME
spring.datasource.password=DB_PASSWORD

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> 실제 Database 비밀번호나 API Key 등의 민감한 정보는 GitHub에 업로드하지 않는 것을 권장합니다.

---

# 테스트 계정

관리자 기능 테스트를 위한 기본 계정입니다.

| 구분       | 값          |
| -------- | ---------- |
| ID       | `admin`    |
| Password | `admin123` |
| 권한       | 관리자        |

---

# 실행 순서

프로젝트를 처음 실행하는 경우 다음 순서로 실행합니다.

```text
1. Backend 실행
       ↓
2. Frontend 실행
       ↓
3. http://localhost:5173 접속
       ↓
4. 관리자 계정 로그인
       ↓
5. 통합 포털 시스템 사용
```

관리자 계정:

```text
ID: admin
PW: admin123
```

---

# 시스템 구성

```text
┌─────────────────┐
│      React      │
│    Frontend     │
└────────┬────────┘
         │
         │ REST API
         ▼
┌─────────────────┐
│   Spring Boot   │
│     Backend     │
└────────┬────────┘
         │
         │ JPA
         ▼
┌─────────────────┐
│   PostgreSQL    │
│    Supabase     │
└─────────────────┘
```

---

### 사용한 설계 도구

* 메뉴 구조도 : Octopus.do
* 업무 프로세스 : draw.io
* 화면 설계 : Figma
* ERD : dbdiagram.io
* 기능 명세 및 문서 관리 : Notion

---
