# Auditorium Domain Rules

## 1. 문서 개요

| 항목 | 설명 |
|---|---|
| **목적** | `auditorium` 도메인의 비즈니스 규칙을 단일 문서로 관리한다. |
| **기준** | 구현 코드에 존재하는 실제 규칙만 기록한다. |
| **포함 범위** | `Auditorium`, `AuditoriumName`, `AuditoriumSeats`, `AuditoriumStatusAndDeletionPolicy`, `AuditoriumTheaterExistencePolicy`, `AuditoriumTheaterActivationStatus`, `AuditoriumTheaterExistenceStatus`, `AuditoriumRepository`, `AuditoriumDomainException` |
| **제외 범위** | API 요청/응답 스펙, 인프라 구현 상세(JPA 쿼리/DB 인덱스 등) |

---

## 2. 패키지 구조

```
auditorium/
└── domain/
    ├── aggregate/
    │   ├── Auditorium.java
    │   ├── AuditoriumName.java
    │   ├── AuditoriumSeats.java
    │   ├── AuditoriumType.java
    │   ├── AuditoriumStatusChange.java
    │   └── AuditoriumRepository.java
    ├── policy/
    │   ├── AuditoriumNameDuplicateValidator.java
    │   ├── AuditoriumStatusAndDeletionPolicy.java
    │   ├── AuditoriumTheaterExistencePolicy.java
    │   └── status/
    │       ├── AuditoriumScreeningLinkStatus.java
    │       ├── AuditoriumTheaterActivationStatus.java
    │       └── AuditoriumTheaterExistenceStatus.java
    ├── port/
    │   ├── AuditoriumJpaPort.java
    │   ├── CheckAuditoriumScreeningLinkPort.java
    │   └── LoadAuditoriumTheaterActivationStatusPort.java
    └── exceptions/
        └── AuditoriumDomainException.java
```

---

## 3. 핵심 규칙

### 3.1 등록/수정 규칙

| 규칙 ID | 규칙 | 실패 메시지 |
|---|---|---|
| AUD-RULE-001 | `theaterId > 0` 이어야 한다. | 영화관 ID는 0보다 커야 합니다. |
| AUD-RULE-002 | 상영관 이름은 필수다. | 상영관 이름은 필수입니다. |
| AUD-RULE-003 | 상영관 이름은 50자 이하여야 한다. | 상영관 이름은 50자를 초과할 수 없습니다. |
| AUD-RULE-004 | 동일 영화관(`theaterId`) 내 상영관 이름은 중복될 수 없다. | '{name}' 이름의 상영관이 해당 영화관에 이미 존재합니다. |
| AUD-RULE-005 | 좌석 목록/행/열은 `AuditoriumSeats` 규칙을 만족해야 한다. | AuditoriumSeats 규칙 메시지 참조 |
| AUD-RULE-017 | 등록 정책(`AuditoriumTheaterExistencePolicy`)은 필수다. | 상영관 등록 정책은 필수입니다. |
| AUD-RULE-019 | 등록 정책 생성 시 영화관 존재 상태 정보는 필수다. | 영화관 존재 상태 정보는 필수입니다. |
| AUD-RULE-018 | 존재하지 않는 영화관에는 상영관을 등록할 수 없다. | 존재하지 않는 영화관에는 상영관을 등록할 수 없습니다. |

### 3.2 좌석 규칙 (`AuditoriumSeats`)

| 규칙 ID | 규칙 | 실패 메시지 |
|---|---|---|
| AUD-RULE-006 | 좌석 목록은 비어 있을 수 없다. | 최소 하나 이상의 좌석이 필요합니다. |
| AUD-RULE-007 | 좌석 수는 `rowCount * columnCount`와 같아야 한다. | 좌석 수가 행과 열의 곱과 일치하지 않습니다. |
| AUD-RULE-008 | 좌석 코드는 중복될 수 없다. | 중복된 좌석이 있습니다. |
| AUD-RULE-009 | `rowCount`는 1 이상 100 이하여야 한다. | 행 수는 0보다 커야 합니다. / 행 수는 100을 초과할 수 없습니다. |
| AUD-RULE-010 | `columnCount`는 1 이상 50 이하여야 한다. | 열 수는 0보다 커야 합니다. / 열 수는 50을 초과할 수 없습니다. |

### 3.3 활성 상태 변경/삭제 규칙

| 규칙 ID | 규칙 | 실패 메시지 |
|---|---|---|
| AUD-RULE-011 | 상태 변경 요청은 필수다. | 변경할 상영관 활성 상태는 필수입니다. |
| AUD-RULE-012 | 활성 상태 변경 정책은 필수다. | 상영관 활성 상태 변경 정책은 필수입니다. |
| AUD-RULE-013 | 비활성화 시 차단 상영이 있으면 실패한다. | 예정/판매중/판매종료 상영이 존재하는 상영관은 비활성화할 수 없습니다. |
| AUD-RULE-014 | 활성화 시 연결된 영화관 정보가 없으면 실패한다. | 영화관 정보를 찾을 수 없습니다. |
| AUD-RULE-015 | 활성화 시 연결된 영화관이 비활성이면 실패한다. | 비활성화된 영화관의 상영관은 활성화할 수 없습니다. |
| AUD-RULE-016 | 삭제 시 차단 상영이 있으면 실패한다. | 예정/판매중/판매종료 상영이 존재하는 상영관은 삭제할 수 없습니다. |

---

## 4. 상태 전이

| 요청 | 결과 |
|---|---|
| `ACTIVATE` | 정책 검증 후 `active = true` |
| `DEACTIVATE` | 정책 검증 후 `active = false` |

---

## 5. 포트 의존

- 이름 중복 검증: `AuditoriumJpaPort.existsByTheaterIdAndName(theaterId, name)`
- 상영 연결 상태 조회: `CheckAuditoriumScreeningLinkPort.loadAuditoriumScreeningLinkStatus(auditoriumId)`
- 연결 영화관 활성 상태 조회: `LoadAuditoriumTheaterActivationStatusPort.loadAuditoriumTheaterActivationStatus(theaterId)` → `AuditoriumTheaterActivationStatus`
- 등록 대상 영화관 존재 확인: `LoadAuditoriumTheaterActivationStatusPort.loadAuditoriumTheaterActivationStatus(theaterId)` 조회 결과를 `AuditoriumTheaterExistenceStatus`로 변환해 사용
