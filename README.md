# 📝 Diary App - 카카오 OAuth2 + JWT 로그인 시스템

Spring Boot 기반의 카카오 OAuth2 연동 로그인 시스템입니다. JWT 토큰을 사용하여 인증을 처리합니다.

## 🚀 주요 기능

- **카카오 OAuth2 로그인**: 카카오 계정으로 간편 로그인
- **JWT 토큰 인증**: Stateless 인증 시스템
- **사용자 정보 관리**: 카카오 사용자 정보 자동 저장/업데이트
- **RESTful API**: 프론트엔드와 연동 가능한 API 제공

## 🛠 기술 스택

- **Backend**: Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Database**: MySQL (개발), H2 (테스트)
- **Authentication**: OAuth2 Client, JWT (jjwt 0.12.3)
- **Build Tool**: Gradle

## 📋 사전 요구사항

- Java 17 이상
- MySQL 8.0 이상 (또는 H2 데이터베이스)
- 카카오 개발자 계정

## 🔧 카카오 개발자 설정

### 1. 카카오 개발자 계정 생성
1. [Kakao Developers](https://developers.kakao.com) 접속
2. 카카오 계정으로 로그인
3. "내 애플리케이션" → "애플리케이션 추가하기"
4. 앱 이름: "Diary App" 입력

### 2. 플랫폼 설정
1. "플랫폼" → "Web 플랫폼 등록"
2. 사이트 도메인: `http://localhost:3000`
3. "저장" 클릭

### 3. 카카오 로그인 활성화
1. "카카오 로그인" → "활성화 설정"
2. Redirect URI: `http://localhost:3000/login/oauth2/code/kakao`
3. "저장" 클릭

### 4. 동의항목 설정
1. "동의항목" → "필수 동의항목"
2. 닉네임, 프로필 사진, 이메일 선택
3. "저장" 클릭

### 5. API 키 확인
1. "앱 키" 탭에서 확인
2. **REST API 키** (Client ID)
3. **Client Secret** (보안 → Client Secret 생성)

## ⚙️ 환경 설정

### 1. 환경 변수 설정
```bash
# 카카오 API 키
export KAKAO_CLIENT_ID="your-kakao-client-id"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"

# JWT 시크릿 키 (최소 256비트)
export JWT_SECRET="your-jwt-secret-key-here-make-it-long-and-secure-at-least-256-bits"
```

### 2. 데이터베이스 설정
MySQL 데이터베이스를 생성하세요:
```sql
CREATE DATABASE diary_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 🚀 실행 방법

### 1. 프로젝트 빌드
```bash
./gradlew clean build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 브라우저에서 접속
```
http://localhost:3000
```

## 🧪 테스트

### 단위 테스트 실행
```bash
# JWT 유틸리티 테스트
./gradlew test --tests JwtUtilTest

# 전체 테스트
./gradlew test
```

### API 테스트
1. 브라우저에서 `http://localhost:3000` 접속
2. "카카오로 로그인" 버튼 클릭
3. 카카오 로그인 완료 후 JWT 토큰 확인
4. API 테스트 버튼으로 기능 확인

## 📡 API 엔드포인트

### 인증 관련 API
- `GET /api/auth/login` - 로그인 정보 조회
- `GET /api/auth/me` - 현재 사용자 정보 조회 (인증 필요)

### OAuth2 엔드포인트
- `GET /oauth2/authorization/kakao` - 카카오 로그인 시작
- `GET /login/oauth2/code/kakao` - 카카오 로그인 콜백

## 🔐 보안 설정

### JWT 토큰
- **알고리즘**: HS256
- **만료 시간**: 24시간 (설정 가능)
- **클레임**: kakaoId, nickname

### Spring Security
- CSRF 비활성화 (JWT 사용)
- 세션 비활성화 (Stateless)
- OAuth2 로그인 활성화

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/cheolhyeon/diary/
│   │   ├── config/           # 설정 클래스
│   │   ├── controller/       # REST 컨트롤러
│   │   ├── entity/          # JPA 엔티티
│   │   ├── repository/      # 데이터 접근 계층
│   │   ├── security/        # 보안 관련 클래스
│   │   ├── service/         # 비즈니스 로직
│   │   └── util/            # 유틸리티 클래스
│   └── resources/
│       ├── static/          # 정적 파일
│       └── application.yaml # 설정 파일
└── test/                    # 테스트 코드
```

## 🔄 동작 흐름

1. **로그인 요청**: 사용자가 카카오 로그인 버튼 클릭
2. **OAuth2 인증**: 카카오 인증 서버로 리다이렉트
3. **사용자 승인**: 카카오에서 사용자 정보 승인
4. **코드 발급**: 인증 코드를 백엔드로 전송
5. **토큰 교환**: 액세스 토큰으로 사용자 정보 조회
6. **사용자 저장**: 데이터베이스에 사용자 정보 저장/업데이트
7. **JWT 발급**: JWT 토큰 생성 및 응답
8. **인증 완료**: 프론트엔드에서 JWT 토큰 사용

## 🐛 문제 해결

### 일반적인 문제들

1. **데이터베이스 연결 오류**
   - MySQL 서비스가 실행 중인지 확인
   - 데이터베이스 이름과 접속 정보 확인

2. **카카오 로그인 오류**
   - 카카오 개발자 설정 확인
   - Redirect URI 설정 확인
   - 환경 변수 설정 확인

3. **JWT 토큰 오류**
   - JWT_SECRET 환경 변수 설정 확인
   - 토큰 만료 시간 확인

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request # Diary
