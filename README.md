# spring-gift-test

Spring Boot 기반 선물/상품 관리 시스템.

## 요구사항

- Java 21
- Docker (테스트용 PostgreSQL)

## 실행

```bash
./gradlew bootRun
```

개발 환경에서는 H2 인메모리 DB를 사용합니다.

## 테스트

```bash
./gradlew cucumberTest
```

Docker가 실행 중이어야 합니다. `spring-boot-docker-compose`가 PostgreSQL 컨테이너를 자동으로 시작/종료합니다.
