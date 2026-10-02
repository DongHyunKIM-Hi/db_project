# 온라인 서점 주문 시스템 — 종합 실습 시작 코드

JPA 엔티티 매핑 · 연관관계와 Repository · 트랜잭션 세 수업을 하나로 묶는 종합 실습 프로젝트입니다.
교안의 **STEP 0(프로젝트 준비)까지 끝난 상태**에서 시작합니다.

## 시작하기

1. PostgreSQL에 `db_project` 데이터베이스를 **새로 생성**합니다.

   ```sql
   CREATE DATABASE db_project;
   ```

2. `src/main/resources/application.yml`의 `username` / `password`를 본인 PostgreSQL 계정에 맞게 수정합니다.
   (기본값: `postgres` / `postgres`)
3. 교안의 STEP 1부터 순서대로 진행합니다.

## 브랜치

| 브랜치 | 내용 |
|---|---|
| `main` | 시작 코드 (STEP 0 완료) |
| `step-1-entities` | STEP 1 정답 — 엔티티 6개 매핑 |
| `step-2-relations` | STEP 2 정답 — 연관관계 |
| `step-3-repository` | STEP 3 정답 — Repository · 도서 API · 시드 데이터 |
| `step-4-order-no-tx` | STEP 4 정답 — 트랜잭션 없는 주문 (사고 재현 상태) |
| `step-5-transactional` | STEP 5 정답 — `@Transactional` · 조회 API · 예외 처리 |
| `step-6-cancel` | STEP 6 정답 — 주문 취소 |
| `step-7-complete` | STEP 7 — 전체 흐름 확인용 HTTP 요청 모음 |
| `challenge-b-nplus1` | 도전 정답 — N+1 해결 (fetch join + batch size) |
| `challenge-propagation` | 도전 정답 — `REQUIRES_NEW`로 주문 시도 이력 남기기 |

막히면 해당 STEP 브랜치와 내 코드를 비교해 보세요. 정답과 달라도 **동작 결과가 요구사항을 만족하면 괜찮습니다.**
