# pulse — den 백엔드 (Spring Boot)

`den`(프로젝트 관리 + 메신저 사내 솔루션)의 백엔드 API 서버.
프로젝트명 `pulse` — den에 박동을 넣는다는 의미. 패키지 루트는 `com.den.pulse`.

## 실행 제약 (중요)
- Gradle 의존성을 임의로 추가·변경하지 말 것. 필요하면 **사용자에게 좌표만 알려줄 것**.
- 대화형 CLI(Spring Initializr, gradle init 등)는 사용자가 직접 실행한다.
- 임시 폴더(/tmp, AppData/Temp) 사용 금지. 프로젝트 폴더 안에서만 작업.
- 기존 문서(CLAUDE.md, docs/)는 절대 삭제·수정하지 말 것.
- 브라우저 자동화 도구를 사용하지 말 것. API 확인은 사용자가 직접 한다.
- DB를 직접 조작하지 말 것(psql 실행 등). 스키마는 JPA 엔티티와 마이그레이션으로만.

## 지금 단계의 목표

**API-SPEC.md의 계약을 그대로 구현하는 것.**

프론트엔드(Vue)는 이미 8단계 전부 완성되어 목업으로 동작 중이다.
`docs/API-SPEC.md`는 그 프론트가 실제로 기대하는 요청·응답 형태를 확정한 **계약서**다.
백엔드는 이 계약을 따라 구현하고, 임의로 응답 형태를 바꾸지 않는다.
계약을 바꿔야 할 이유가 생기면 **먼저 사용자에게 알리고 합의한 뒤** API-SPEC.md를 갱신한다.

---

## 기술 스택 (확정 — 변경 금지)

- **Spring Boot 4.1.0 / Java 25** (Gradle - Kotlin DSL)
- **Spring Web MVC** — REST API (WebFlux 아님. 동기 MVC)
- **Spring Data JPA** + **PostgreSQL**
- **Spring Security** + **JWT** (jjwt)
- **Spring Data Redis** — WS 세션, 알림 큐
- **Spring WebSocket + STOMP** — 메신저 실시간
- **Spring Validation** — 요청 검증
- **Lombok**

> Boot 4는 스타터가 모듈화되어 `spring-boot-starter-web`이 아니라
> `spring-boot-starter-webmvc`를 쓴다. 테스트도 모듈별 `-test` 스타터를 쓴다.
> 3.x 시절 예제를 그대로 가져오지 말고 4.x 기준으로 작성할 것.

---

## 반드시 지킬 규칙

### 1. 검증은 프론트·백엔드 양쪽 모두 (가장 중요)
프론트에 검증이 있다고 해서 백엔드가 생략하지 않는다.
- **클라이언트에서 온 값은 신뢰 대상이 아니다.** 전송 중 변조·유실될 수 있다.
- 모든 요청 DTO에 `@Valid` + Bean Validation 애너테이션을 건다.
- 비즈니스 규칙(날짜 역전, 순환 참조, 중복 멤버 등)도 서비스 계층에서 다시 검증한다.
- "프론트에서 체크했으니 여기선 로직만" 은 금물.

### 2. 권한 판단은 전부 서버에서
`docs/DEN-DESIGN.md` 5장 참고. 요약:
- 프로젝트 하위 리소스는 요청자가 **멤버인지** 확인 → 아니면 **404** (403 아님, 존재 은닉)
- `isPrivate` 업무는 담당자·참여자가 아니면 목록·상세 모두 **404**
- 메뉴 권한(tasks/gantt/messenger)이 꺼진 역할은 **403**
- 프론트는 숨기기만 할 뿐, 최종 판단은 항상 서버.

### 3. 식별자 규칙
- 프로젝트는 URL에 **`projectKey`**(사람이 읽는 키, 예 "APP")를 쓴다. **내부 UUID를 URL에 노출하지 않는다.**
- 그 외 리소스(taskId, userId, roleId, channelId 등)는 UUID를 그대로 쓴다.

### 4. 응답 형식 (API-SPEC.md 0.3절)
- 래퍼(`{ data: ... }`) 없이 리소스를 그대로 반환.
- 목록은 배열 그대로. **단, 페이지네이션이 있는 목록만** `{ items, total, page, size }`.
- 없는 리소스 → **404**. 부수효과만 있고 바디 불필요 → **204**.
- 날짜: `startDate`/`endDate`/`invitedAt`은 `YYYY-MM-DD`, 그 외는 ISO 8601 datetime.

### 5. 트랜잭션·부수효과
API-SPEC.md의 **비고(서버 side effect)** 를 빠뜨리지 말 것. 예:
- 프로젝트 생성 → 요청자용 관리자 역할 자동 생성 + 멤버 등록
- 댓글 작성 → `commentCount` 증가 + 멘션/댓글 알림 생성
- 담당자 변경 → 새로 추가된 사람에게 `task_assigned` 알림
- 상태를 `done`으로 변경 → `progress`를 100으로 자동 설정
- 알림 생성 트리거 전체 목록은 API-SPEC.md 7장 표 참고.

### 6. 패키지 구조 (도메인 기반)
```
com.den.pulse
├── core/            기술 기반 (예외, 응답, 설정, 유틸)
│   ├── config/      Security, WebSocket, Jpa, Redis 설정
│   ├── exception/   전역 예외 처리 (@RestControllerAdvice)
│   └── security/    JWT 발급·검증, 인증 필터, 현재 사용자 조회
└── domain/          업무 영역
    ├── auth/            로그인·토큰 재발급·내 정보
    ├── user/            사용자
    ├── project/         프로젝트, 폴더, 즐겨찾기, 배치
    ├── member/          프로젝트 멤버, 역할, 메뉴 권한
    ├── task/            업무, 담당자, 의존성, 하위업무, 태그, 댓글
    ├── channel/         채널, 메시지, 읽음 처리
    └── notification/    알림
```
각 도메인 아래는 `controller / service / repository / entity / dto` 로 나눈다.

### 7. JPA 사용 규칙 (중요 — 반드시 준수)

**7-1. INSERT/UPDATE 판단은 코드가 한다. `save()`에 맡기지 않는다.**
`save()`는 내부적으로 SELECT를 날려 존재 여부를 확인한 뒤 INSERT/UPDATE를 결정한다.
이 방식은 불필요한 SELECT를 남발하고, 어떤 쿼리가 나가는지 코드에서 안 보인다.
- 생성이 확실한 경우 → `EntityManager.persist()` 또는 그에 준하는 명시적 생성
- 수정인 경우 → **먼저 조회**하고, 그 엔티티를 수정
- 멱등성이 필요한 경우 → **코드가 직접 조회하고 분기**한다. JPA에 판단을 위임하지 않는다.

**7-2. `@DynamicUpdate` 사용 금지.**
변경 필드를 런타임에 비교해 UPDATE 문을 동적 생성하는 방식은 쓰지 않는다.
항상 동일한 UPDATE 문이 나가야 예측 가능하다.

**7-3. 부분 수정(PATCH)은 "조회 → 필요한 필드만 세팅 → 전체 UPDATE".**
```java
// 올바름
Task task = taskRepository.findById(taskId).orElseThrow(...);  // 권한 체크에도 필요한 조회
if (request.hasTitle())    task.setTitle(request.getTitle());
if (request.hasPriority()) task.setPriority(request.getPriority());
// ... 요청에 들어온 필드만 세팅 → 전체 컬럼 UPDATE
```
- `COALESCE(:v, column)` 식의 "null이면 기존값 유지" 쿼리는 쓰지 않는다.
  **값을 의도적으로 null로 지우는 경우를 표현할 수 없기 때문**이다.
- 요청 DTO는 "필드가 왔는지"와 "값이 null인지"를 구분할 수 있어야 한다
  (Optional 래핑, 별도 플래그 등 — 구현 방식은 사용자와 합의).

**7-4. 조회는 필요한 만큼만.**
- 권한 체크로 이미 조회한 엔티티가 있으면 재조회하지 않는다.
- 연관관계는 기본 `LAZY`. 필요한 곳에서 fetch join으로 명시적으로 가져온다.
- N+1이 발생할 지점은 미리 짚고 fetch join 또는 배치 조회로 해결한다.

---

## 작업 순서 (이 순서대로 진행)

1. **공통 토대** — 설정(application.yml, DB/Redis 연결), 전역 예외 처리, 공통 응답 규약
2. **엔티티 전체** — `docs/DEN-DESIGN.md` 4.1절 테이블 전부. 관계가 얽혀 있으므로 한 번에.
   - DB는 로컬 Docker PostgreSQL 사용
   - 스키마는 **`spring.jpa.hibernate.ddl-auto: update`로 자동 생성**한다 (개발 단계)
   - 엔티티 구조가 안정되면 Flyway 마이그레이션으로 전환 예정 (그때 사용자와 합의)
   - **운영 환경에서는 `ddl-auto`를 절대 `update`/`create`로 두지 않는다**
3. **인증** — JWT 발급·검증, Security 설정, `/api/auth/*`
4. **프로젝트·폴더·즐겨찾기** — `/api/projects`, `/api/folders`, 배치·즐겨찾기
5. **멤버·역할·권한** — 멤버 CRUD, 역할, 메뉴 권한. **권한 체크 공통 로직도 여기서 확립**
6. **업무** — 목록(필터·페이지네이션), 상세, 수정, 담당자, 의존성, 하위업무, 태그
7. **댓글·알림** — 댓글 + 알림 생성 트리거 전체
8. **메신저** — 채널·메시지·읽음 + STOMP 실시간

**한 번에 하나씩.** 각 단계 완료 후 사용자에게 확인받고 진행할 것.

> 5.5절(역할 정의 범위)의 절충안은 **이 단계에서 확정**한다.
> `PROJECT_ROLE.project_id`를 nullable로 두어 전역 기본 역할 + 프로젝트 예외 구조로 간다.
> 5단계 진행 시 사용자와 먼저 합의할 것.

---

## 하지 말 것

- ❌ WebFlux·리액티브 사용 (동기 MVC로 확정)
- ❌ Spring Boot 3.x 문법·설정 그대로 가져오기 (4.x 기준으로 작성)
- ❌ API-SPEC.md 계약을 임의로 변경 (바꿔야 하면 먼저 사용자와 합의)
- ❌ 프론트 검증을 믿고 백엔드 검증 생략
- ❌ **`save()`로 INSERT/UPDATE 판단 위임** (7-1)
- ❌ **`@DynamicUpdate` 사용** (7-2)
- ❌ **`COALESCE(:v, column)` 식 부분 업데이트 쿼리** (7-3)
- ❌ 권한 체크를 컨트롤러마다 중복 구현 (공통화할 것)
- ❌ 엔티티를 그대로 응답으로 반환 (반드시 DTO 변환)
- ❌ URL에 프로젝트 UUID 노출
- ❌ 한 번에 여러 단계 진행 (한 단계씩, 확인받고 진행)

---

## 참고 문서

- `docs/API-SPEC.md` — **구현 계약** (엔드포인트·요청·응답·타입·부수효과). 가장 먼저 볼 것.
- `docs/DEN-DESIGN.md` — 설계 맥락 (데이터 모델 4장, 권한 모델 5장, WebSocket 6.2절, 결정 로그 9장)

프론트는 별도 저장소(`den`)에 있으며 목업으로 동작 중이다.
백엔드 완성 후 연동하며, 그때 프론트에서 함께 고쳐야 할 항목은 API-SPEC.md 10장에 정리되어 있다.

응답은 한국어로 한다.
