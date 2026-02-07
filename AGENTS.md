# Domain Rule Boundary

도메인 규칙/정책은 반드시 도메인 계층에 선언한다.  
절대로 핸들러/인프라 계층에서 규칙을 구현하지 않는다.

## Operating Principles

- 핸들러는 오케스트레이션만 수행한다: 조회(getById), 도메인 메서드 호출, 저장(save)
- 상태 전이 선택/분기(`if`, `switch`)는 aggregate 또는 domain policy에서만 수행한다
- 인프라 계층은 영속화/외부 연동만 담당하고 비즈니스 판단을 하지 않는다

## Allowed Exceptions

- 기술적 방어 코드(매핑/직렬화/널 체크)는 허용한다
- 비즈니스 규칙 판단(상태 전이, 정책 검증, 도메인 예외 판단)은 허용하지 않는다

## Cross-Domain Dependency Rule

이 규칙은 비가역(Non-negotiable) 원칙이다.

- `A.domain.. -> B.domain..` 직접 import를 금지한다.
- 교차 도메인 규칙은 반드시 소비 도메인이 포트를 선언한다.
- 포트 구현은 반드시 소비 도메인의 인프라 어댑터에서 수행한다.
- 도메인 예외/정책 판단은 도메인 계층에서만 수행한다.

허용 예시:

- `movie.domain.policy.port.CheckMovieScreeningLinkPort`
- `movie.infrastructure.policy.CheckMovieScreeningLinkJpaAdapter`

금지 예시:

- `movie.domain.*`에서 `screening.domain.aggregate.*` 직접 참조
- 핸들러/인프라에서 정책 판단(`if`, `switch`)으로 도메인 규칙 구현

에이전트 작업 절차(고정):

- 교차 규칙 필요 시 다음 순서를 반드시 지킨다.
- 도메인 포트 먼저 정의
- 도메인 정책에서 포트 사용
- 인프라 어댑터 구현

## Domain Rule Documentation Sync

- 도메인 영역 변경 시(aggregate/policy/value object/domain exception) 해당 도메인 규칙 문서를 반드시 최신화한다.
- 문서는 구현 코드에 존재하는 실제 규칙만 기록한다.
- 문서 양식은 `src/main/java/com/movie/shop/api/screening/domain/SCREENING_DOMAIN_RULES.md`를 기준으로 맞춘다.

## PR Review Checklist

- 핸들러/인프라에 상태 전이 분기나 비즈니스 판단 코드가 없는가
- 도메인 aggregate/policy가 규칙을 소유하는가
- 도메인 예외는 도메인 계층에서 발생하는가
- `domain` 패키지가 타 도메인 `domain` 패키지를 직접 참조하지 않는가
- 도메인 규칙이 변경되었을 때 도메인 규칙 문서도 함께 갱신되었는가

## Commit Message Rule

- 커밋 메시지는 `Type(scope): 한글 메시지` 형식으로 작성한다.
- `Type`은 앞글자만 대문자, 나머지는 소문자로 작성한다. 예: `Feat`, `Fix`, `Refactor`
- `scope`는 선택 사항이며, 없으면 `Type: 한글 메시지` 형식을 사용한다.
- `:` 뒤에는 공백 한 칸을 둔다.
- 메시지 본문은 한글로 작성한다(기술 식별자/용어는 영문 사용 가능).
