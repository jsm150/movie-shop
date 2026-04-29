# Domain Rule Boundary

도메인 규칙은 반드시 도메인 계층에 선언한다.  
절대로 핸들러/인프라 계층에서 규칙을 구현하지 않는다.

## Operating Principles

- 핸들러는 오케스트레이션만 수행한다: 조회(getById), 도메인 메서드 호출, 저장(save)
- 핸들러는 도메인 메서드 호출에 필요한 포트 조회/데이터 조립을 수행할 수 있다(비즈니스 분기/판단 없이 전달만 수행).
- 포트는 소비 도메인의 언어로 표현된 상태/조건을 반환한다.
- 외부 상태로 판단해야 하는 규칙은 핸들러가 조회한 상태를 aggregate/value object/domain service에 전달하고, 도메인 모델이 직접 판단한다.
- 상태 전이 선택/분기(`if`, `switch`)는 aggregate/value object/domain service에서만 수행한다.
- domain policy 객체는 필수가 아니다. 여러 aggregate/value object를 가로지르는 복잡한 규칙 조합이 있을 때만 도입한다.
- 단순히 포트 조회 결과를 감싸서 위임하는 policy 객체는 만들지 않는다.
- 인프라 계층은 영속화/외부 연동만 담당하고 비즈니스 판단을 하지 않는다

## Allowed Exceptions

- 기술적 방어 코드(매핑/직렬화/널 체크)는 허용한다
- 비즈니스 규칙 판단(상태 전이, 조건 검증, 도메인 예외 판단)은 허용하지 않는다

## Cross-Domain Dependency Rule

이 규칙은 비가역(Non-negotiable) 원칙이다.

- `A.domain.. -> B.domain..` 직접 import를 금지한다.
- 교차 도메인 규칙은 반드시 소비 도메인이 포트를 선언한다.
- 포트 구현은 반드시 소비 도메인의 인프라 어댑터에서 수행한다.
- 도메인 예외/규칙 판단은 소비 도메인의 도메인 계층에서만 수행한다.

허용 예시:

- `screening.domain.port.MovieSchedulingConditionPort`
- `screening.infrastructure.policy.MovieSchedulingConditionJpaAdapter`

금지 예시:

- `movie.domain.*`에서 `screening.domain.aggregate.*` 직접 참조
- 핸들러/인프라에서 조건 판단(`if`, `switch`)으로 도메인 규칙 구현

에이전트 작업 절차(고정):

- 교차 규칙 필요 시 다음 순서를 반드시 지킨다.
- 소비 도메인에 포트 먼저 정의
- 포트 반환 타입은 소비 도메인의 상태/조건 값 객체로 정의
- 인프라 어댑터는 외부 도메인을 조회해 상태/조건 값 객체로 매핑만 수행
- 핸들러는 포트 조회 결과를 도메인 메서드에 전달
- aggregate/value object/domain service가 상태/조건을 기반으로 규칙 판단

## PR Review Checklist

- 핸들러/인프라에 상태 전이 분기나 비즈니스 판단 코드가 없는가
- 도메인 aggregate/value object/domain service가 규칙을 소유하는가
- 단순 위임용 policy 객체가 새로 생기지 않았는가
- 도메인 예외는 도메인 계층에서 발생하는가
- `domain` 패키지가 타 도메인 `domain` 패키지를 직접 참조하지 않는가

## Commit Message Rule

- 커밋 메시지는 `Type(scope): 한글 메시지` 형식으로 작성한다.
- `Type`은 앞글자만 대문자, 나머지는 소문자로 작성한다. 예: `Feat`, `Fix`, `Refactor`
- `scope`는 선택 사항이며, 없으면 `Type: 한글 메시지` 형식을 사용한다.
- `:` 뒤에는 공백 한 칸을 둔다.
- 메시지 본문은 한글로 작성한다(기술 식별자/용어는 영문 사용 가능).
