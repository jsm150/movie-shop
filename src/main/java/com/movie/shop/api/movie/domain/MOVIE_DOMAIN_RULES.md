# Movie Domain Rules

## 1. 문서 개요

| 항목 | 설명 |
|---|---|
| **목적** | `movie` 도메인의 비즈니스 규칙을 단일 문서로 관리한다. |
| **기준** | 구현 코드에 존재하는 실제 규칙만 기록한다. |
| **포함 범위** | `Movie`, `MovieTitle`, `MovieRepository`, `MovieDeletionPolicy`, `MovieTitleDuplicateValidator`, `MovieDomainException` |
| **제외 범위** | API 요청/응답 스펙, 인프라 구현 상세(JPA 쿼리/인덱스), 테스트 전용 시나리오 |

---

## 2. 패키지 구조

```
movie/
├── api/
│   └── commands/                               ← 커맨드 핸들러 (오케스트레이션만 수행)
├── domain/
│   ├── aggregate/
│   │   ├── Movie.java                          ← 핵심 Aggregate Root
│   │   ├── MovieTitle.java                     ← 제목 VO
│   │   ├── MovieStatus.java                    ← 영화 상태 enum
│   │   ├── MovieStateChange.java               ← 상태 변경 요청 enum
│   │   └── MovieRepository.java                ← 도메인 리포지토리
│   ├── policy/
│   │   ├── MovieDeletionPolicy.java            ← 삭제 정책
│   │   ├── MovieTitleDuplicateValidator.java   ← 제목 중복 정책
│   │   ├── MovieScreeningLinkStatus.java       ← 상영 연결 조회 new type
│   │   └── MovieTitleDuplication.java          ← 제목 중복 조회 new type
│   ├── port/
│   │   ├── MovieJpaPort.java                   ← 영화 영속화/중복 조회 포트
│   │   └── CheckMovieScreeningLinkPort.java    ← 상영 연결 여부 조회 포트
│   └── exceptions/
│       └── MovieDomainException.java
└── infrastructure/
    ├── persistence/
    └── policy/
```

---

## 3. 상태/전이 규칙

### 3.1 영화 상태 (`MovieStatus`)

| 상태 | 의미 |
|---|---|
| `PREPARING` | 준비 중 (등록 직후 초기 상태) |
| `COMING_SOON` | 상영 예정 |
| `NOW_SHOWING` | 상영 중 |
| `ENDED` | 상영 종료 |

### 3.2 상태 변경 요청 (`MovieStateChange`)

| 요청 | 호출 메서드 |
|---|---|
| `COMING_SOON` | `moveToComingSoon()` |
| `NOW_SHOWING` | `startShowing()` |
| `ENDED` | `endShowing()` |

### 3.3 상태 전이 제약

| 메서드 | 허용 출발 상태 | 실패 메시지 |
|---|---|---|
| `moveToComingSoon()` | `PREPARING` | PREPARING 이 아닌 상태에서 COMING_SOON으로 변경하려고 함. |
| `startShowing()` | `COMING_SOON` | COMING_SOON 이 아닌 상태에서 NOW_SHOWING으로 변경하려고 함. |
| `endShowing()` | `NOW_SHOWING` | NOW_SHOWING 이 아닌 상태에서 ENDED로 변경하려고 함. |

`changeState(MovieStateChange)`는 상태 변경 요청을 위 3개 메서드로 라우팅하며, 입력이 `null`이면 예외를 발생시킨다.

---

## 4. 등록/수정 규칙

### 4.1 등록 (`Movie.Register`)

| 규칙 ID | 조건 | 실패 메시지 |
|---|---|---|
| MOV-RULE-001 | 제목 필수/길이(최대 200) | 영화 제목은 필수입니다. / 영화 제목은 200자를 초과할 수 없습니다. |
| MOV-RULE-002 | 제목 중복 불가 | '<title>' 제목을 가진 영화가 이미 존재합니다. |
| MOV-RULE-003 | 감독 필수, 길이 100 이하 | 감독 이름은 필수입니다. / 감독 이름은 100자를 초과할 수 없습니다. |
| MOV-RULE-004 | 장르 최소 1개 + 각 항목 공백 불가 | 최소 하나 이상의 장르가 필요합니다. / 장르는 빈 값이나 공백을 포함할 수 없습니다. |
| MOV-RULE-005 | `runtimeMinutes > 0` | 상영 시간은 0보다 커야 합니다. |
| MOV-RULE-006 | 관람등급 필수 | 유효하지 않은 관람 등급입니다. |
| MOV-RULE-007 | 시놉시스 필수, 길이 1000 이하 | 시놉시스는 필수입니다. / 시놉시스는 1000자를 초과할 수 없습니다. |
| MOV-RULE-008 | 개봉일 필수 | 개봉일은 필수입니다. |
| MOV-RULE-009 | 출연진 최소 1명 | 최소 한 명 이상의 출연진이 필요합니다. |

- 등록 시 상태는 항상 `PREPARING`으로 설정된다.

### 4.2 수정 (`Movie.Update`)

- 제목은 `MovieTitle.createFrom(...)` 규칙을 따른다.
- 기존 제목과 신규 제목이 같으면 중복 검증을 생략한다.
- 나머지 필드는 등록과 동일한 Bean Validation 규칙을 따른다.

---

## 5. 삭제 규칙

### 5.1 삭제 정책 (`MovieDeletionPolicy`)

| 규칙 ID | 조건 | 실패 메시지 |
|---|---|---|
| MOV-RULE-010 | `NOW_SHOWING` 상태는 삭제 불가 | NOW_SHOWING 상태의 영화는 삭제할 수 없습니다. |
| MOV-RULE-011 | `movie.id != null` | 영화 ID가 존재하지 않습니다. |
| MOV-RULE-012 | 상영 연결이 있으면 삭제 불가 | 상영이 연결된 영화는 삭제할 수 없습니다. |

### 5.2 오케스트레이션 규칙

- `DeleteMovieCommandHandler`는 다음 순서만 수행한다.
1. `MovieRepository.getById(...)`
2. `CheckMovieScreeningLinkPort.loadMovieScreeningLinkStatus(...)`
3. `MovieDeletionPolicy` 생성
4. `MovieRepository.delete(...)`

- 핸들러는 비즈니스 분기(`if`, `switch`)를 수행하지 않는다.

---

## 6. 정책 입력 New Type 규칙

### 6.1 제목 중복 정책 입력

- `MovieJpaPort.loadTitleDuplication(String title)`는 `MovieTitleDuplication`을 반환한다.
- `MovieTitleDuplicateValidator`는 `MovieTitleDuplication`을 생성자 주입받아 판단한다.

### 6.2 삭제 정책 입력

- `CheckMovieScreeningLinkPort.loadMovieScreeningLinkStatus(long movieId)`는 `MovieScreeningLinkStatus`를 반환한다.
- `MovieDeletionPolicy`는 `MovieScreeningLinkStatus`를 생성자 주입받아 판단한다.

### 6.3 계층 책임

- 포트 구현(infrastructure)은 조회/매핑만 수행한다.
- 정책은 new type을 기반으로 도메인 판단만 수행한다.
- raw 타입(`boolean`) 직접 주입은 허용하지 않는다.

---

## 7. 교차 도메인 의존 규칙

- `movie.domain..`은 `screening.domain..`을 직접 참조하지 않는다.
- 상영 연결 여부는 `movie` 도메인 포트(`CheckMovieScreeningLinkPort`)를 통해서만 조회한다.
- 포트 구현은 `movie.infrastructure.policy.CheckMovieScreeningLinkJpaAdapter`가 담당한다.
