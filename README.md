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

## Git 관리 정책
- `build/` 디렉토리 Git ignore 처리
- `.gradle/` 디렉토리 Git ignore 처리
- 불필요한 빌드 결과물 커밋 금지

## API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| GET | /api/trips | 여행 목록 조회 |
| POST | /api/trips | 여행 생성 |
| GET | /api/trips/{id} | 여행 상세 조회 |
| GET | /api/trips/{id}/settlement | 정산 결과 조회 |
| POST | /api/trips/{id}/expenses | 지출 추가 |

---
*2026-01-07 기준 모든 프롬프트 적용 완료*
