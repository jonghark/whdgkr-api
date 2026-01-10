# whdgkr-api

TripSplite 앱의 Spring Boot API 서버

## 기술 스택
- Spring Boot 3.2
- Java 17
- JPA / Hibernate
- MySQL
- Flyway (DB 마이그레이션)

## 실행 방법
```bash
# Java 17 설정 필요
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# 서버 실행
./gradlew bootRun
```

## 책임 범위
- 비즈니스 로직
- 데이터 검증
- 계산 / 정산 로직
- DB 트랜잭션
- 인증 / 보안

## 프로젝트 구조
```
whdgkr-api/
├── src/main/java/com/whdgkr/tripsplite/
│   ├── controller/    # REST API 컨트롤러
│   ├── service/       # 비즈니스 로직
│   ├── repository/    # 데이터 접근
│   ├── entity/        # JPA 엔티티
│   └── dto/           # 데이터 전송 객체
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/  # Flyway 마이그레이션
└── build.gradle
```

## 도메인 규칙

### Soft Delete 패턴
- 모든 엔티티는 `deleteYn` 필드 사용 ('Y'/'N')
- 조회 시 `deleteYn='N'` 조건 적용

### 동행자(Participant) 규칙
- owner는 삭제 불가
- 동행자 카운트는 deleteYn='N' 기준

## API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| GET | /api/trips | 여행 목록 조회 |
| POST | /api/trips | 여행 생성 |
| GET | /api/trips/{id} | 여행 상세 조회 |
| PATCH | /api/trips/{id} | 여행 일정 수정 |
| DELETE | /api/trips/{id} | 여행 삭제 |
| POST | /api/trips/{id}/participants | 동행자 추가 |
| DELETE | /api/trips/{id}/participants/{pid} | 동행자 삭제 |
| POST | /api/trips/{id}/expenses | 지출 추가 |
| PUT | /api/trips/expenses/{id} | 지출 수정 |
| DELETE | /api/trips/expenses/{id} | 지출 삭제 |
| GET | /api/trips/{id}/settlement | 정산 결과 조회 |
| POST | /api/dev/reset | 데이터 전체 초기화 (개발 전용) |
| GET | /api/dev/stats | 데이터 통계 조회 (개발 전용) |

## Git 관리 정책
- `build/` 디렉토리 Git ignore 처리
- `.gradle/` 디렉토리 Git ignore 처리
- 불필요한 빌드 결과물 커밋 금지

## 문서 관리 정책
- 모든 문서는 CLAUDE.md 하나로 관리
- README.md, CHANGELOG.md 별도 생성 금지
- 변경 이력은 아래 Changelog 섹션에 날짜별 누적

---

## Changelog

### 2026-01-07
- 지출 수정 API 경로/메서드 정합성 수정 (PUT /api/trips/expenses/{id})
- 여행 일정 수정 API 추가 (PATCH /api/trips/{id})

### 2026-01-08
- 동행자 수 집계 기준 통일 (deleteYn='N')
- 문서 정책 통합 (CLAUDE.md 단일화)
- 여행 일정 수정 시 지출 날짜 검증 추가
  - 지출 날짜가 새 기간 밖으로 밀려나는 변경 시 409 CONFLICT 반환

### 2026-01-10
- 개발자 도구 API 추가
  - POST /api/dev/reset: 전체 데이터 초기화 (앱 최초 설치 상태 복원)
  - GET /api/dev/stats: 데이터 통계 조회
  - DELETE 방식 사용 (TRUNCATE/DROP 절대 금지)
  - DevController, DevService 추가
