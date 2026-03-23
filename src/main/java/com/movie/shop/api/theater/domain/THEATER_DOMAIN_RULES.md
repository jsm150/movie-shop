# Theater Domain Rules

## 1. 문서 개요

| 항목 | 설명 |
|---|---|
| **목적** | `theater` 도메인의 비즈니스 규칙을 단일 문서로 관리한다. |
| **기준** | 구현 코드에 존재하는 실제 규칙만 기록한다. |
| **포함 범위** | `Theater`, `TheaterName`, `TheaterRepository`, `TheaterAuditoriumLinkProtectionPolicy`, `TheaterAuditoriumLinkStatus`, `TheaterDomainException` |
| **제외 범위** | API 요청/응답 스펙, 인프라 구현 상세(JPA 쿼리/DB 인덱스 등) |

---

## 2. 패키지 구조

```
theater/
└── domain/
    ├── aggregate/
    │   ├── Theater.java
    │   ├── TheaterName.java
    │   ├── TheaterActiveChange.java
    │   └── TheaterRepository.java
    ├── policy/
    │   ├── TheaterNameDuplicateValidator.java
    │   ├── TheaterAuditoriumLinkProtectionPolicy.java
    │   └── TheaterAuditoriumLinkStatus.java
    ├── port/
    │   ├── TheaterJpaPort.java
    │   └── CheckTheaterAuditoriumLinkPort.java
    └── exceptions/
        └── TheaterDomainException.java
```

---

## 3. 핵심 규칙

### 3.1 등록/수정 규칙

| 규칙 ID | 규칙 | 실패 메시지 |
|---|---|---|
| THR-RULE-001 | 영화관 이름은 필수다. | 영화관 이름은 필수입니다. |
| THR-RULE-002 | 영화관 이름은 50자 이하여야 한다. | 영화관 이름은 50자를 초과할 수 없습니다. |
| THR-RULE-003 | 영화관 이름은 중복될 수 없다. | '{name}' 이름의 영화관이 이미 존재합니다. |

### 3.2 활성 상태 변경/삭제 규칙

| 규칙 ID | 규칙 | 실패 메시지 |
|---|---|---|
| THR-RULE-004 | 상태 변경 요청은 필수다. | 변경할 영화관 활성 상태는 필수입니다. |
| THR-RULE-005 | 활성 상태 변경 정책은 필수다. | 영화관 활성 상태 변경 정책은 필수입니다. |
| THR-RULE-006 | 연결된 상영관이 있으면 비활성화할 수 없다. | 연결된 상영관이 존재하는 영화관은 비활성화할 수 없습니다. |
| THR-RULE-007 | 연결된 상영관이 있으면 삭제할 수 없다. | 연결된 상영관이 존재하는 영화관은 삭제할 수 없습니다. |
| THR-RULE-008 | 영화관의 상영 가능 여부(`canHostScreening`)는 `active` 상태와 동일하다. | - |

---

## 4. 상태 전이

| 요청 | 결과 |
|---|---|
| `ACTIVATE` | `active = true` |
| `DEACTIVATE` | 정책 검증 후 `active = false` |

---

## 5. 포트 의존

- 이름 중복 검증: `TheaterJpaPort.existsByName(name)`
- 연결 상영관 상태 조회: `CheckTheaterAuditoriumLinkPort.loadTheaterAuditoriumLinkStatus(theaterId)`
