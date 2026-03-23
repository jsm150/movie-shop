# Screening Domain Rules

## 1. 문서 개요

| 항목 | 설명 |
|---|---|
| **목적** | `screening` 도메인의 비즈니스 규칙을 단일 문서로 관리한다. |
| **기준** | 구현 코드에 존재하는 실제 규칙만 기록한다. |
| **포함 범위** | `Screening`, `ScreeningTimeRange`, `SalesTimeRange`, `ScreeningScheduleValidationPolicy(record)`, `ScreeningConflictValidationPolicy(record)`, `ScreeningTimeRuntimeValidationPolicy(record)`, `ScreeningRepository`, `ScreeningDomainException` |
| **제외 범위** | API 요청/응답 스펙, 인프라 구현 상세(JPA 쿼리 최적화, DB 인덱스 등) |

---

## 2. 패키지 구조

```
screening/
├── api/
│   └── commands/                          ← 커맨드 핸들러 (오케스트레이션만 수행)
├── domain/
│   ├── aggregate/
│   │   ├── Screening.java                 ← 핵심 Aggregate Root
│   │   ├── ScreeningStatus.java           ← 상영 상태 enum
│   │   ├── ScreeningStateChange.java      ← 상태 변경 요청 enum
│   │   ├── ScreeningTimeRange.java        ← 상영 시간 범위 VO
│   │   ├── SalesTimeRange.java            ← 판매 시간 범위 VO
│   │   └── ScreeningRepository.java       ← 도메인 리포지토리
│   ├── policy/
│   │   ├── ScreeningScheduleValidationPolicy.java      ← 영화/상영관 일정 검증 정책(record)
│   │   ├── ScreeningConflictValidationPolicy.java      ← 시간 충돌 검증 정책(record)
│   │   └── ScreeningTimeRuntimeValidationPolicy.java   ← 상영 시간 런타임 검증 정책(record)
│   ├── port/
│   │   ├── ScreeningJpaPort.java                    ← 영속화 포트
│   │   ├── LoadMovieSchedulingAvailabilityPort.java   ← 영화 스케줄 가능/런타임 확인 포트
│   │   ├── MovieSchedulingAvailability.java           ← 영화 스케줄 가능/런타임 조회 모델
│   │   ├── LoadAuditoriumScreeningAvailabilityPort.java  ← 상영관 가용 확인 포트
│   │   └── AuditoriumScreeningAvailability.java          ← 상영관 가용 조회 모델
│   └── exceptions/
│       └── ScreeningDomainException.java  ← 도메인 예외
└── infrastructure/
    ├── persistence/                       ← JPA 어댑터
    └── policy/                            ← 교차 도메인 포트 어댑터
```

---

## 3. 용어 정의

### 3.1 상영 상태 (`ScreeningStatus`)

| 상태 | 의미 | 설명 |
|---|---|---|
| `SCHEDULED` | 상영 예정 | 등록 직후 초기 상태. 수정/삭제 가능 |
| `ON_SALE` | 판매 중 | 티켓 판매가 진행 중인 상태 |
| `SALES_CLOSED` | 판매 종료 | 티켓 판매가 마감된 상태 |
| `CANCELED` | 상영 취소 | 취소된 상태. 최종 상태(되돌릴 수 없음) |
| `FINISHED` | 상영 종료 | 상영이 완료된 상태. 최종 상태(되돌릴 수 없음) |

### 3.2 상태 변경 요청 (`ScreeningStateChange`)

| 요청 | 호출 메서드 | 설명 |
|---|---|---|
| `OPEN_SALES` | `openSales(now)` | 판매 시작 |
| `CLOSE_SALES` | `closeSales()` | 판매 종료 |
| `CANCEL` | `cancel(reason, now)` | 상영 취소 |
| `FINISH` | `finish(now)` | 상영 종료 |

---

## 4. 상태 전이 규칙

### 4.1 상태 머신 다이어그램

```
                  OPEN_SALES               CLOSE_SALES              FINISH
  ┌───────────┐  (now≥salesStart)  ┌─────────┐              ┌──────────────┐            ┌──────────┐
  │ SCHEDULED ├───────────────────►│ ON_SALE ├─────────────►│ SALES_CLOSED ├───────────►│ FINISHED │
  └─────┬─────┘                    └────┬────┘              └──────┬───────┘            └──────────┘
        │                               │                         │
        │          CANCEL               │        CANCEL           │      CANCEL
        │      (reason필수, now필수)     │    (reason필수, now필수) │  (reason필수, now필수)
        │                               │                         │
        ▼                               ▼                         ▼
  ┌──────────┐◄─────────────────────────┘◄────────────────────────┘
  │ CANCELED │
  └──────────┘
```

### 4.2 상태 전이 표

| 이벤트 | 허용 출발 상태 | 시간 조건 | 결과 상태 |
|---|---|---|---|
| `OPEN_SALES` | `SCHEDULED` | `now >= salesStartAt` | `ON_SALE` |
| `CLOSE_SALES` | `ON_SALE` | 없음 | `SALES_CLOSED` |
| `CANCEL` | `SCHEDULED`, `ON_SALE`, `SALES_CLOSED` | `now != null` | `CANCELED` |
| `FINISH` | `SALES_CLOSED` | `now >= screeningEndTime` | `FINISHED` |

### 4.3 경계값 허용

- `OPEN_SALES`: `now == salesStartAt` → **허용**
- `FINISH`: `now == screeningEndTime` → **허용**

### 4.4 최종 상태

- `CANCELED`와 `FINISHED`는 최종 상태로, 어떤 전이도 불가능하다.

---

## 5. 작업별 규칙 상세

### 5.1 등록 (`Screening.register`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-001 | `schedulePolicy != null` | 상영 일정 검증 정책은 필수입니다. |
| 2 | SCR-RULE-032 | `conflictPolicy != null` | 상영 시간 충돌 검증 정책은 필수입니다. |
| 3 | SCR-RULE-031 | `runtimePolicy != null` | 상영 시간 런타임 검증 정책은 필수입니다. |
| 4 | SCR-RULE-002 | 정책 검증: 영화 → 상영관 | (정책 내부 메시지 참조) |
| 5 | SCR-RULE-003 | 초기 상태 `SCHEDULED` 설정 | - |
| 6 | - | `ScreeningTimeRange`, `SalesTimeRange` VO 검증(판매시간+충돌) | (VO/정책 내부 메시지 참조) |
| 7 | - | Bean Validation (`movieId`, `theaterId`, 필수값) | (Bean Validation 메시지 참조) |

### 5.2 수정 (`Screening.reschedule`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-004 | `schedulePolicy != null` | 상영 일정 검증 정책은 필수입니다. |
| 2 | SCR-RULE-032 | `conflictPolicy != null` | 상영 시간 충돌 검증 정책은 필수입니다. |
| 3 | SCR-RULE-031 | `runtimePolicy != null` | 상영 시간 런타임 검증 정책은 필수입니다. |
| 4 | SCR-RULE-005 | `id != null` | 상영 ID가 존재하지 않아 일정 변경 검증을 수행할 수 없습니다. |
| 5 | - | 정책 검증: 영화 → 상영관 | (정책 내부 메시지 참조) |
| 6 | SCR-RULE-006 | `status == SCHEDULED` | SCHEDULED 상태의 상영만 일정 변경이 가능합니다. |
| 7 | - | `ScreeningTimeRange`, `SalesTimeRange` VO 검증(판매시간+충돌) | (VO/정책 내부 메시지 참조) |

### 5.3 삭제 (`ScreeningRepository.removeScheduledById`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-029 | 상영 존재 확인 (`getById`) | 상영 정보를 찾을 수 없습니다. |
| 2 | SCR-RULE-019 | `status == SCHEDULED` | SCHEDULED 상태의 상영만 삭제할 수 있습니다. |

### 5.4 판매 시작 (`openSales`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-007 | `status == SCHEDULED` | 판매 시작은 SCHEDULED 상태에서만 가능합니다. |
| 2 | SCR-RULE-008 | `now != null` | 현재 시간은 필수입니다. |
| 3 | SCR-RULE-009 | `now >= salesStartAt` | 판매 시작 시간 이전에는 판매를 시작할 수 없습니다. |

### 5.5 판매 종료 (`closeSales`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-010 | `status == ON_SALE` | 판매 종료는 ON_SALE 상태에서만 가능합니다. |

### 5.6 상영 취소 (`cancel`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-011 | `status != CANCELED && status != FINISHED` | 종료 상태의 상영은 취소할 수 없습니다. |
| 2 | SCR-RULE-012 | `reason`이 비어있지 않음 | 취소 사유는 필수입니다. |
| 3 | SCR-RULE-013 | `reason.length() <= 200` | 취소 사유는 200자 이하여야 합니다. |
| 4 | SCR-RULE-014 | `now != null` | 현재 시간은 필수입니다. |

### 5.7 상영 종료 (`finish`)

| 순서 | 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|---|
| 1 | SCR-RULE-015 | `status == SALES_CLOSED` | SALES_CLOSED 상태의 상영만 종료할 수 있습니다. |
| 2 | SCR-RULE-016 | `now != null` | 현재 시간은 필수입니다. |
| 3 | SCR-RULE-017 | `now >= screeningEndTime` | 상영 종료 시간 이전에는 상영을 종료할 수 없습니다. |

### 5.8 상태 변경 라우팅 (`changeState`)

| 규칙 ID | 검증 | 실패 메시지 |
|---|---|---|
| SCR-RULE-018 | `stateChange != null` | 변경할 상영 상태는 필수입니다. |

`changeState`는 `ScreeningStateChange` 값에 따라 위 5.4~5.7 메서드로 위임한다.

---

## 6. 시간 범위 규칙

### 6.1 상영 시간 (`ScreeningTimeRange`)

| 규칙 ID | 조건 | 실패 메시지 |
|---|---|---|
| SCR-RULE-023 | `startTime < endTime` | 상영 시작 시간은 상영 종료 시간 이전 이여야 합니다. |
| SCR-RULE-030 | `startTime/endTime` 유효 시 `상영 구간 >= runtimeMinutes` | 상영 시간은 영화 런타임(%d분) 이상이어야 합니다. |
| - | `startTime != null` | 상영 시작 시간이 필요합니다. |
| - | `endTime != null` | 상영 종료 시간이 필요합니다. |
| - | `runtimePolicy != null` | 상영 시간 런타임 검증 정책은 필수입니다. |

### 6.2 판매 시간 (`SalesTimeRange`)

```
   salesStartAt        salesEndAt     screeningStartAt        screeningEndTime
       │                   │                │                       │
       ├───── 판매 구간 ────┤                │                       │
       │                   │◄── 같거나 이전 ─┤                       │
       │                   │                ├──── 상영 구간 ─────────┤
       ▼                   ▼                ▼                       ▼
  ─────●───────────────────●────────────────●───────────────────────●──────► 시간
```

| 규칙 ID | 조건 | 실패 메시지 |
|---|---|---|
| SCR-RULE-024 | `salesStartAt < salesEndAt` | 판매 시작 시간은 판매 종료 시간보다 이전이어야 합니다. |
| SCR-RULE-025 | `salesEndAt <= screeningStartAt` | 판매 종료 시간은 상영 시작 시간보다 늦을 수 없습니다. |
| - | `salesStartAt != null` | 판매 시작 시간은 필수입니다. |
| - | `salesEndAt != null` | 판매 종료 시간은 필수입니다. |
| - | `screeningStartAt != null` | 상영 시작 시간은 필수입니다. |
| - | `screeningEndAt != null` | 상영 종료 시간은 필수입니다. |
| SCR-RULE-032 | `conflictPolicy != null` | 상영 시간 충돌 검증 정책은 필수입니다. |
| SCR-RULE-028 | `conflictPolicy.validateNoConflict(screeningStartAt, screeningEndAt)` | 동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다. |

---

## 7. 시간 충돌 규칙

### 7.1 충돌 판정

| 규칙 ID | 내용 |
|---|---|
| SCR-RULE-022 | 반열림 구간 겹침 판정: `start < targetEnd && targetStart < end` |
| SCR-RULE-021 | `CANCELED` 상태의 상영은 충돌 판정에서 제외 (항상 `false`) |

### 7.2 충돌 판정 예시

```
  기존 상영:     10:00 ──────── 12:00
  후보 A:              11:00 ──────── 13:00   → 충돌 ✓  (10:00 < 13:00 && 11:00 < 12:00)
  후보 B:   08:00 ── 10:00                    → 충돌 ✗  (10:00 < 10:00 = false)
  후보 C:                      12:00 ── 14:00 → 충돌 ✗  (12:00 < 12:00 = false)
  후보 D:        09:00 ────── 11:00           → 충돌 ✓  (10:00 < 11:00 && 09:00 < 12:00)
```

---

## 8. 부가 판정 규칙

| 규칙 ID | 메서드 | 조건 | 설명 |
|---|---|---|---|
| SCR-RULE-020 | `blocksTheaterDeactivationOrDeletion()` | `SCHEDULED`, `ON_SALE`, `SALES_CLOSED` → `true` | 해당 상태의 상영이 존재하면 영화관 비활성화/삭제를 차단한다. |
| SCR-RULE-029 | `ScreeningRepository.getById(id)` | 조회 결과 없음 | 상영 정보를 찾을 수 없습니다. |

---

## 9. 교차 도메인 정책 (`ScreeningScheduleValidationPolicy`, `ScreeningConflictValidationPolicy`, `ScreeningTimeRuntimeValidationPolicy`)

### 9.1 의존 구조

```
Register/UpdateScreeningCommandHandler
  ├── LoadMovieSchedulingAvailabilityPort   → (movie 스케줄 가능 여부 + runtimeMinutes 조회)
  ├── LoadAuditoriumScreeningAvailabilityPort  → (auditorium 가용 여부 조회)
  └── ScreeningJpaPort                      → (충돌 후보 조회)
            │
            ▼
ScreeningScheduleValidationPolicy(record, preloaded data)
ScreeningConflictValidationPolicy(record, preloaded data)
ScreeningTimeRuntimeValidationPolicy(record, preloaded data)
```

> ⚠️ **교차 도메인 규칙**: `screening.domain`은 `movie.domain`이나 `theater.domain`을 직접 참조하지 않는다. 포트 인터페이스 기반 조회 결과만 정책(record)에 전달한다.

### 9.2 검증 순서

| 순서 | 규칙 ID | 검증 대상 | 조건 | 실패 메시지 |
|---|---|---|---|---|
| 1 | SCR-RULE-026 | 영화 존재 여부 | 조회 결과 있음 | 영화 정보를 찾을 수 없습니다. |
| 2 | SCR-RULE-026 | 영화 스케줄 가능 상태 | `COMING_SOON` 또는 `NOW_SHOWING` | 상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다. |
| 3 | SCR-RULE-027 | 상영관 존재 여부 | 조회 결과 있음 | 상영관 정보를 찾을 수 없습니다. |
| 4 | SCR-RULE-027 | 상영관 상영 가능 상태 | 활성화 상태 | 활성화된 상영관에서만 상영 등록/수정이 가능합니다. |
| 5 | SCR-RULE-028 | 시간 충돌 여부 | 후보 중 충돌 없음 | 동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다. |

- 위 검증 데이터는 핸들러가 포트로 선조회한 값을 세 정책 record 생성자에 주입한다.

### 9.3 상영 시간 런타임 정책

| 순서 | 규칙 ID | 검증 대상 | 조건 | 실패 메시지 |
|---|---|---|---|---|
| 1 | SCR-RULE-026 | 영화 존재 여부 | 조회 결과 있음 | 영화 정보를 찾을 수 없습니다. |
| 2 | SCR-RULE-030 | 영화 런타임 연계 | `상영 구간 >= runtimeMinutes` | 상영 시간은 영화 런타임(%d분) 이상이어야 합니다. |

- `ScreeningTimeRuntimeValidationPolicy`는 런타임 연계 검증만 담당한다.
- `ScreeningScheduleValidationPolicy`는 영화/상영관 스케줄 가능 상태 검증만 소유한다.
- `ScreeningConflictValidationPolicy`는 시간 충돌 검증만 소유한다.
- `Screening.register/reschedule`는 일정 정책 검증 후 `SalesTimeRange.create(..., conflictPolicy)`에서 충돌 검증을 수행하고, `ScreeningTimeRange.create(..., runtimePolicy)`에서 런타임 검증을 수행한다.

### 9.4 등록 vs 수정 차이

| 구분 | 등록 (`validateCanCreate`) | 수정 (`validateCanReschedule`) |
|---|---|---|
| 충돌 후보 범위 | 해당 상영관의 전체 후보 | 자기 자신을 제외한 후보 |
| 호출 메서드 | `findConflictCandidatesByTheaterId` | `findConflictCandidatesByTheaterIdAndIdNot` |

---

## 10. Bean Validation 제약

`Screening` 엔티티에 선언된 Bean Validation 어노테이션 기반 제약이다.

| 필드 | 어노테이션 | 메시지 |
|---|---|---|
| `movieId` | `@Positive` | 영화 ID는 0보다 커야 합니다. |
| `theaterId` | `@Positive` | 상영관 ID는 0보다 커야 합니다. |
| `screeningTimeRange` | `@NotNull` | 상영 시간 범위는 필수입니다. |
| `salesTimeRange` | `@NotNull` | 판매 시간 범위는 필수입니다. |
| `status` | `@NotNull` | 상영 상태는 필수입니다. |
| `cancelReason` | `@Size(max=200)` | 취소 사유는 200자 이하여야 합니다. |

---

## 11. 규칙 ID 전체 인덱스

### 11.1 규칙 ID → 메시지 매핑

| ID | 규칙명 | 실패 메시지 |
|---|---|---|
| SCR-RULE-001 | 등록 시 정책 필수 | 상영 일정 검증 정책은 필수입니다. |
| SCR-RULE-002 | 등록 시 사전 정책 검증 | (정책 내부 메시지) |
| SCR-RULE-003 | 등록 초기 상태 고정 | - (항상 `SCHEDULED`) |
| SCR-RULE-004 | 수정 시 정책 필수 | 상영 일정 검증 정책은 필수입니다. |
| SCR-RULE-005 | 수정 시 식별자 필수 | 상영 ID가 존재하지 않아 일정 변경 검증을 수행할 수 없습니다. |
| SCR-RULE-006 | 수정 시 상태 제한 | SCHEDULED 상태의 상영만 일정 변경이 가능합니다. |
| SCR-RULE-007 | 판매 시작 상태 제한 | 판매 시작은 SCHEDULED 상태에서만 가능합니다. |
| SCR-RULE-008 | 판매 시작 현재시각 필수 | 현재 시간은 필수입니다. |
| SCR-RULE-009 | 판매 시작 시점 제한 | 판매 시작 시간 이전에는 판매를 시작할 수 없습니다. |
| SCR-RULE-010 | 판매 종료 상태 제한 | 판매 종료는 ON_SALE 상태에서만 가능합니다. |
| SCR-RULE-011 | 취소 상태 제한 | 종료 상태의 상영은 취소할 수 없습니다. |
| SCR-RULE-012 | 취소 사유 필수 | 취소 사유는 필수입니다. |
| SCR-RULE-013 | 취소 사유 길이 제한 | 취소 사유는 200자 이하여야 합니다. |
| SCR-RULE-014 | 취소 현재시각 필수 | 현재 시간은 필수입니다. |
| SCR-RULE-015 | 상영 종료 상태 제한 | SALES_CLOSED 상태의 상영만 종료할 수 있습니다. |
| SCR-RULE-016 | 상영 종료 현재시각 필수 | 현재 시간은 필수입니다. |
| SCR-RULE-017 | 상영 종료 시점 제한 | 상영 종료 시간 이전에는 상영을 종료할 수 없습니다. |
| SCR-RULE-018 | 상태 변경 요청 필수 | 변경할 상영 상태는 필수입니다. |
| SCR-RULE-019 | 삭제 가능 상태 제한 | SCHEDULED 상태의 상영만 삭제할 수 있습니다. |
| SCR-RULE-020 | 영화관 비활성/삭제 차단 판정 | - (판정 메서드, 예외 없음) |
| SCR-RULE-021 | 충돌 시 취소 상영 제외 | - (판정 메서드, 예외 없음) |
| SCR-RULE-022 | 시간 충돌 판정식 | - (판정 메서드, 예외 없음) |
| SCR-RULE-023 | 상영 시간 범위 유효성 | 상영 시작 시간은 상영 종료 시간 이전 이여야 합니다. |
| SCR-RULE-024 | 판매 시간 범위 유효성(1) | 판매 시작 시간은 판매 종료 시간보다 이전이어야 합니다. |
| SCR-RULE-025 | 판매 시간 범위 유효성(2) | 판매 종료 시간은 상영 시작 시간보다 늦을 수 없습니다. |
| SCR-RULE-026 | 영화 스케줄 가능 정책 | 영화 정보를 찾을 수 없습니다. / 상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다. |
| SCR-RULE-027 | 상영관 스케줄 가능 정책 | 상영관 정보를 찾을 수 없습니다. / 활성화된 상영관에서만 상영 등록/수정이 가능합니다. |
| SCR-RULE-028 | 상영 시간 충돌 정책(`ScreeningConflictValidationPolicy`) | 동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다. |
| SCR-RULE-029 | 상영 조회 실패 | 상영 정보를 찾을 수 없습니다. |
| SCR-RULE-030 | 상영 시간 런타임 연계 정책 | 상영 시간은 영화 런타임(%d분) 이상이어야 합니다. |
| SCR-RULE-031 | 런타임 정책 필수 | 상영 시간 런타임 검증 정책은 필수입니다. |
| SCR-RULE-032 | 충돌 정책 필수 | 상영 시간 충돌 검증 정책은 필수입니다. |

### 11.2 예외 메시지 → 규칙 ID 역매핑

| 메시지 | 규칙 ID |
|---|---|
| 상영 일정 검증 정책은 필수입니다. | SCR-RULE-001, SCR-RULE-004 |
| 상영 시간 충돌 검증 정책은 필수입니다. | SCR-RULE-032 |
| 상영 시간 런타임 검증 정책은 필수입니다. | SCR-RULE-031 |
| 상영 ID가 존재하지 않아 일정 변경 검증을 수행할 수 없습니다. | SCR-RULE-005 |
| SCHEDULED 상태의 상영만 일정 변경이 가능합니다. | SCR-RULE-006 |
| 판매 시작은 SCHEDULED 상태에서만 가능합니다. | SCR-RULE-007 |
| 현재 시간은 필수입니다. | SCR-RULE-008, SCR-RULE-014, SCR-RULE-016 |
| 판매 시작 시간 이전에는 판매를 시작할 수 없습니다. | SCR-RULE-009 |
| 판매 종료는 ON_SALE 상태에서만 가능합니다. | SCR-RULE-010 |
| 종료 상태의 상영은 취소할 수 없습니다. | SCR-RULE-011 |
| 취소 사유는 필수입니다. | SCR-RULE-012 |
| 취소 사유는 200자 이하여야 합니다. | SCR-RULE-013 |
| SALES_CLOSED 상태의 상영만 종료할 수 있습니다. | SCR-RULE-015 |
| 상영 종료 시간 이전에는 상영을 종료할 수 없습니다. | SCR-RULE-017 |
| 변경할 상영 상태는 필수입니다. | SCR-RULE-018 |
| SCHEDULED 상태의 상영만 삭제할 수 있습니다. | SCR-RULE-019 |
| 상영 시작 시간은 상영 종료 시간 이전 이여야 합니다. | SCR-RULE-023 |
| 판매 시작 시간은 판매 종료 시간보다 이전이어야 합니다. | SCR-RULE-024 |
| 판매 종료 시간은 상영 시작 시간보다 늦을 수 없습니다. | SCR-RULE-025 |
| 영화 정보를 찾을 수 없습니다. | SCR-RULE-026 |
| 상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다. | SCR-RULE-026 |
| 상영관 정보를 찾을 수 없습니다. | SCR-RULE-027 |
| 활성화된 상영관에서만 상영 등록/수정이 가능합니다. | SCR-RULE-027 |
| 동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다. | SCR-RULE-028 |
| 상영 정보를 찾을 수 없습니다. | SCR-RULE-029 |
| 상영 시간은 영화 런타임(%d분) 이상이어야 합니다. | SCR-RULE-030 |

---

## 12. 테스트 근거

| 검증 대상 | 테스트 파일 |
|---|---|
| Aggregate 규칙 | `screening/domain/aggregate/ScreeningTest.java` |
| 상영 시간 VO + 런타임 검증 규칙 | `screening/domain/aggregate/ScreeningTimeRangeTest.java` |
| 일정 정책 규칙(영화/상영관) | `screening/domain/policy/ScreeningScheduleValidationPolicyTest.java` |
| 충돌 정책 규칙 | `screening/domain/policy/ScreeningConflictValidationPolicyTest.java` |
| 런타임 정책 규칙 | `screening/domain/policy/ScreeningTimeRuntimeValidationPolicyTest.java` |
| 상태 전이 + 시각 통합 | `screening/api/commands/ChangeStateScreeningCommandHandlerIntegrationTest.java` |
| 등록 회귀(런타임 미달/범위 우선순위 포함) | `screening/api/commands/RegisterScreeningCommandHandlerIntegrationTest.java` |
| 수정 회귀(런타임 미달 포함) | `screening/api/commands/UpdateScreeningCommandHandlerIntegrationTest.java` |
| 삭제 회귀 | `screening/api/commands/DeleteScreeningCommandHandlerIntegrationTest.java` |

> 테스트 경로 기준: `src/test/java/com/movie/shop/api/`

---

## 13. 문서 유지보수 규칙

- 도메인 규칙 변경(PR) 시 본 문서를 **같은 PR**에서 함께 갱신한다.
- 새로운 `ScreeningDomainException` 메시지가 추가되면 **11.1 규칙 ID 인덱스**와 **11.2 역매핑**에 반드시 추가한다.
- 상태 전이 규칙 변경 시 **4.1 상태 머신 다이어그램**과 **4.2 전이 표**를 함께 갱신한다.
- 새로운 교차 도메인 포트 추가 시 **9.1 의존 구조**를 갱신한다.
