# den API 명세 (백엔드 구현용)

이 문서는 `src/mock/api.ts`(목업 API 함수)와 `src/mock/types.ts`(데이터 타입)를
기준으로, 프론트가 실제로 기대하는 요청·응답 형태를 정리한 것이다.
백엔드는 이 문서의 시그니처를 계약으로 삼아 구현하고, 프론트는 목업 함수를
동일한 시그니처의 axios 호출로 교체하기만 하면 되도록 맞춘다.

상위 설계 맥락(ERD, 권한 모델, WebSocket 구조)은 `docs/DEN-DESIGN.md` 4~6장 참고.
이 문서는 그중 6장을 실제 함수 시그니처 수준으로 구체화한 버전이다.

---

## 0. 공통 규칙

### 0.1 베이스 URL / 인증
- 베이스 경로: `/api`
- 인증: `Authorization: Bearer <JWT>` (로그인 이후 모든 요청에 필요. `/api/auth/*`, 헬스체크 제외)
- 아래 명세에서 인증 헤더는 생략하고 표기한다.

### 0.2 식별자 규칙 (중요)
- **프로젝트는 URL에 `projectKey`(사람이 읽는 키, 예: `"APP"`)를 쓴다. 내부 UUID를 URL에 노출하지 않는다.**
  아래 `{projectKey}`로 표기한 경로 파라미터는 전부 이 값이다.
- 그 외 리소스(`taskId`, `userId`, `roleId`, `channelId`, `folderId`, `notificationId` 등)는 서버 내부 ID(UUID 문자열)를 그대로 쓴다.
- 목업 데이터의 ID 형식(`t-app-142`, `u-jk` 등)은 목업 전용이며, 실제 백엔드는 UUID를 발급하면 된다. 프론트는 ID 값의 형식에 의존하지 않는다.

### 0.3 응답 형식
- 모든 응답은 별도 래퍼(`{ data: ... }` 등) 없이 **리소스를 그대로** JSON으로 반환한다 (목업 함수의 반환값과 동일).
- 목록 조회는 배열을 그대로 반환한다 (`{ items: [...] }` 형태 아님). **단, 페이지네이션이 적용되는 목록은 예외로 `{ items, total, page, size }` 래퍼를 쓴다** (4장 업무 목록 참고. `total`/`page`/`size`가 필요 없는 단순 목록은 원칙대로 배열 그대로).
- 대상이 없는 단건 조회/수정(`PATCH /tasks/{id}` 등)은 **404**를 반환한다. 목업 함수가 `T | undefined`를 반환하는 지점이 여기 해당한다.
- 부수효과만 있고 응답 바디가 필요 없는 요청(읽음 처리 등, 목업 함수가 `void` 반환)은 **204 No Content**로 응답한다.
- 날짜: `startDate`/`endDate`/`invitedAt`은 `YYYY-MM-DD`, 그 외 `createdAt` 등은 ISO 8601 datetime 문자열.

### 0.4 권한 체크 (요약 — 상세는 DEN-DESIGN.md 5장)
- 모든 프로젝트 하위 리소스(`/projects/{projectKey}/...`)는 요청자가 해당 프로젝트 멤버인지 서버가 확인한다. 아니면 **404**(존재 자체를 숨김. 403 아님).
- `isPrivate` 업무는 담당자(`assigneeIds`) 또는 참여자(`watcherIds`)가 아니면 목록·상세 모두 **404**.
- 메뉴 권한(업무/간트/메신저)이 꺼진 역할은 해당 하위 리소스 요청 시 **403**.
- 프론트는 숨기기만 할 뿐 최종 판단은 항상 서버가 한다.

### 0.5 이 문서에 없는 것 (목업 전용, 백엔드 구현 대상 아님)
`src/mock/api.ts`의 `addAutoReply`, `simulateBackgroundActivity`는 실시간 메신저를
시연하기 위한 프론트 전용 시뮬레이션이며 실제 API 대응물이 없다. 백엔드는 대신
8장의 STOMP 메시지 브로드캐스트로 실시간성을 제공한다.

---

## 1. 인증

### `POST /api/auth/login`
로그인 폼(`LoginPage.vue`)이 기대하는 형태. 목업 단계에서는 항상 고정 사용자(`CURRENT_USER_ID`)로 로그인 처리하므로 실제 검증 로직은 없으나, 실제 구현 시 아래 계약을 따른다.

**Request**
```ts
{ email: string; password: string }
```

**Response** `200`
```ts
{ accessToken: string; refreshToken: string; user: User }
```
`401` — 이메일/비밀번호 불일치.

### `GET /api/auth/me`
목업 함수: `fetchCurrentUser(): Promise<User>`

**Response** `200` → [`User`](#user)

### `POST /api/auth/refresh`
리프레시 토큰으로 액세스 토큰을 재발급한다. 설계상 자리만 예약 (목업 미구현).

**MVP 방식**: 리프레시 토큰은 로테이션하지 않는다 — 재발급 요청마다 액세스 토큰만 새로 내려주고, 리프레시 토큰은 로그인 때 발급받은 것을 만료 전까지 그대로 재사용한다.

**Request**
```ts
{ refreshToken: string }
```

**Response** `200`
```ts
{ accessToken: string }
```
`401` — 리프레시 토큰 만료/무효. 프론트는 이 경우 로그아웃 처리 후 `/login`으로 이동한다.

> **향후 과제 (MVP 범위 밖)**: 리프레시 토큰 로테이션(재발급마다 새 리프레시 토큰도 함께 발급하고 이전 토큰은 즉시 무효화) — 탈취된 토큰의 재사용을 탐지·차단할 수 있어 보안상 더 안전하지만, 기존 토큰 무효화 처리와 동시 재발급 요청 처리(같은 리프레시 토큰으로 두 요청이 동시에 들어오는 경우) 같은 구현 복잡도가 있어 MVP 이후로 미룬다.

---

## 2. 프로젝트 / 폴더 / 즐겨찾기

### `GET /api/projects`
목업 함수: `fetchProjects(): Promise<Project[]>`
내가 속한 프로젝트 전체.

**Response** `200` → [`Project`](#project)`[]`

### `POST /api/projects`
목업 함수: `createProject(name: string, folderId?: string | null): Promise<Project>`

**Request**
```ts
{ name: string; folderId?: string | null }
```

**Response** `201` → [`Project`](#project)

**비고 (서버 side effect)**: 생성 시 요청자를 위한 관리자 역할(`isAdmin: true`, 모든 메뉴 권한 `true`)을 자동 생성하고 `ProjectMember`로 등록한다. `key`는 서버가 발급하는 고유 슬러그(예: 프로젝트명 기반 또는 순번). `color`는 팔레트에서 순환 할당. **기본 채널 자동 생성 (2026-08-29 추가)**: "일반"이라는 이름의 그룹 채널(`Channel.type: 'group'`)을 함께 만들고 요청자를 그 채널의 멤버로 등록한다. 이후 초대되는 멤버는 이 기본 채널에 자동 참여하지 않는다(필요해지면 별도 합의 후 추가).

### `GET /api/projects/{projectKey}`
목업 함수: `fetchProjectByKey(key): Promise<Project | undefined>`

**Response** `200` → [`Project`](#project) / `404`

### `GET /api/me/project-roles`
목업 함수: `fetchMyProjectRoles(): Promise<Record<string, Role | undefined>>`
전체 프로젝트 홈 카드의 역할 배지용 — 내가 속한 각 프로젝트에서의 내 역할.

**Response** `200`
```ts
Record<projectId, Role | undefined>
```
> 주의: 이 맵의 키는 `projectId`(내부 ID)다. `GET /api/projects` 응답의 각 `Project.id`와 매칭해서 쓴다.

### 폴더

#### `GET /api/folders`
목업 함수: `fetchFolders(): Promise<Folder[]>` — 요청자 개인 폴더 목록.

**Response** `200` → [`Folder`](#folder)`[]`

#### `POST /api/folders`
목업 함수: `createFolder(name: string): Promise<Folder>`

**Request** `{ name: string }`
**Response** `201` → [`Folder`](#folder)

#### `PATCH /api/projects/{projectKey}/placement`
목업 함수: `moveProjectToFolder(projectId: string, folderId: string | null): Promise<Project | undefined>`
프로젝트를 내 화면에서 다른 폴더로 이동 (개인용 배치이므로 요청자 본인에게만 영향). 식별자는 다른 프로젝트 하위 엔드포인트와 통일해 `projectKey`를 쓴다 (0.2절).

**Request** `{ folderId: string | null }` (`null` = 미분류로 이동)
**Response** `200` → [`Project`](#project) / `404`

### 즐겨찾기

#### `GET /api/favorites`
목업 함수: `fetchFavoriteProjectIds(): Promise<string[]>`

**Response** `200` → `string[]` (즐겨찾기한 `projectId` 목록)

#### `POST /api/projects/{projectKey}/favorite`
목업 함수: `toggleFavoriteProject(projectId: string): Promise<string[]>`
토글(있으면 해제, 없으면 추가).

**Response** `200`
```ts
{ isFavorite: boolean }   // 토글 후 상태
```
> 목업 함수는 갱신된 전체 즐겨찾기 목록(`string[]`)을 반환하지만, 실제 백엔드는 매 토글마다 전체 목록을 다시 내려받지 않도록 이 단건 결과만 반환한다 → 10장 참고.

---

## 3. 사용자 / 멤버 / 권한

> **멤버·역할 관리 권한 (2026-08-29 확정)**
> 아래 표시된 멤버 초대/역할 변경/제거, 역할 메뉴권한 수정 4개 엔드포인트는
> **요청자가 해당 프로젝트에서 관리자 역할(`Role.isAdmin === true`)을 가진 멤버일 때만** 허용한다.
> 관리자가 아니면(멤버이더라도) **403**. 전역(시스템 전체) 관리자 개념은 없음 — 프로젝트마다 독립적으로 판단.
> (0.4절의 멤버 여부 체크는 선행 조건으로 그대로 적용: 아예 멤버가 아니면 404.)

### `GET /api/projects/{projectKey}/users`
목업 함수 대응 없음 (목업의 `fetchUsers()`를 프로젝트 범위로 좁힌 엔드포인트).
멘션 대상·담당자 후보 선택용 — 이 프로젝트 멤버만 반환한다. 댓글 멘션, 담당자 지정, 메신저 멘션 등 "이 프로젝트 안에서 사람을 고르는" 화면은 모두 이 엔드포인트를 쓴다.

**Response** `200` → [`User`](#user)`[]`

### `GET /api/users?q=`
전체 사용자 검색. 아직 이 프로젝트 멤버가 아닌 "기존 계정"을 찾아야 하는 멤버 초대 화면에서만 쓴다 (3장 멤버 초대 참고) — 멘션·담당자 선택에는 쓰지 않는다.

**Query Parameters**
```ts
{ q: string }   // 이름 또는 이메일 부분일치
```

**Response** `200` → [`User`](#user)`[]`

> **목업과의 차이**: 목업 함수 `fetchUsers()`는 전체 사용자를 반환하고, 프론트는 이 하나의 결과를 멘션·담당자 선택과 멤버 초대 검색 양쪽에 그대로 재사용한다. 실제로는 멘션·담당자 선택 범위가 프로젝트 멤버로 좁아져야 하므로 위 두 엔드포인트로 분리했다 → 10장 참고.

### `GET /api/projects/{projectKey}/roles`
목업 함수: `fetchRolesByProjectKey(projectKey): Promise<Role[]>`

**Response** `200` → [`Role`](#role)`[]`

### `GET /api/projects/{projectKey}/members`
목업 함수: `fetchProjectMembers(projectKey): Promise<ProjectMember[]>`

**Response** `200` → [`ProjectMember`](#projectmember)`[]`

### `GET /api/projects/{projectKey}/members/roles`
목업 함수: `fetchProjectMemberRoles(projectKey): Promise<Record<string, Role | undefined>>`
멤버 관리 화면에서 멤버별 현재 역할을 빠르게 조회하기 위한 보조 엔드포인트.

**Response** `200`
```ts
Record<userId, Role | undefined>
```

### `GET /api/projects/{projectKey}/menu-permissions`
목업 함수: `fetchMenuPermissions(projectKey): Promise<Record<MenuKey, boolean>>`
**요청자 본인**의 이 프로젝트 내 메뉴 접근 권한. 컨텍스트 바 탭 표시/숨김에 사용.

**Response** `200`
```ts
Record<'tasks' | 'gantt' | 'messenger', boolean>
```

### `POST /api/projects/{projectKey}/members`
기존 계정을 가진 사용자를 프로젝트 멤버로 초대한다. **초대 과정에서 신규 계정을 생성하지 않는다** — 사내 솔루션이므로 계정 발급·비밀번호 설정은 이 API 밖의 별도 계정 관리 절차(관리자 도구, SSO 프로비저닝 등)에서 이루어진다. 초대는 "이미 존재하는 계정"과 "프로젝트+역할"을 연결하는 동작으로 한정한다.

**Request**
```ts
{ userId: string; roleId: string }
```

**Response** `201` → [`ProjectMember`](#projectmember)

`404` — 존재하지 않는 `userId`. `409` — 이미 해당 프로젝트 멤버. **`403`** — 요청자가 관리자가 아님.

> **목업과의 차이**: 목업 함수 `inviteProjectMember(projectKey, name, email, roleId)`는 이름·이메일을 입력받아 그 자리에서 신규 `User`를 만드는 것처럼 동작한다 → 10장 참고.

> **비고 (서버 side effect, 2026-08-29 확정)**: 초대된 사용자를 그 프로젝트의 **그룹 채널(`type=group`) 전체**에 `CHANNEL_MEMBER`로 함께 추가한다. DM 채널(`type=dm`)은 대상에서 제외 — 원래 참여자 두 명만의 채널이므로 새 멤버를 끼워 넣지 않는다. 이 처리가 없으면 새 멤버는 기존 그룹 채널을 아예 볼 수 없다(메신저 화면에 "채널이 없습니다").

### `PATCH /api/projects/{projectKey}/members/{userId}`
목업 함수: `updateProjectMemberRole(projectKey, userId, roleId): Promise<void>`

**Request** `{ roleId: string }`
**Response** `204`

**`403`** — 요청자가 관리자가 아님.
**`409`** — **마지막 관리자 보호**: 대상이 그 프로젝트의 유일한 관리자 멤버인데 `roleId`가 관리자가 아닌 역할을 가리키는 경우. 관리자가 0명인 프로젝트가 생기는 것을 막는다.

### `DELETE /api/projects/{projectKey}/members/{userId}`
목업 함수: `removeProjectMember(projectKey, userId): Promise<void>`

**Response** `204`

**`403`** — 요청자가 관리자가 아님.
**`409`** — **마지막 관리자 보호**: 대상이 그 프로젝트의 유일한 관리자 멤버인 경우 제거 불가.

### `PATCH /api/roles/{roleId}/menu-permissions`
목업 함수: `updateRoleMenuPermission(roleId, menuKey, value): Promise<void>`

**Request**
```ts
{ menuKey: 'tasks' | 'gantt' | 'messenger'; value: boolean }
```
**Response** `204`

**`403`** — 요청자가 (해당 역할이 속한 프로젝트의) 관리자가 아님.

---

## 4. 업무

### `GET /api/projects/{projectKey}/tasks`
목업 함수: `fetchTasksByProjectKey(projectKey): Promise<Task[]>`
비공개(`isPrivate`) 업무 중 요청자가 담당자/참여자가 아닌 항목은 서버가 제외하고 반환한다.

**Query Parameters** (전부 선택, 미지정 시 필터 없음)
```ts
{
  status?: string     // 콤마 구분, TaskStatus 값들. 예: status=todo,progress
  assignee?: string   // 콤마 구분, 담당자 userId들
  priority?: string   // 콤마 구분, TaskPriority 값들
  tag?: string        // 콤마 구분, tagId들
  q?: string           // 검색어 — 제목 부분일치
  page?: number        // 1-base, 기본 1
  size?: number         // 페이지당 개수, 기본 20
}
```

**Response** `200`
```ts
{
  items: Task[]
  total: number   // 필터 적용 후 전체 개수 (페이지네이션 계산용)
  page: number
  size: number
}
```

> **목업 단계와의 차이**: 목업 함수(`fetchTasksByProjectKey`)는 프로젝트의 전체 업무 배열을 반환하고, 필터·검색·페이지네이션은 `TaskListPage.vue`가 `route.query`를 읽어 **클라이언트에서** 처리한다 → 10장 참고.
> `group`(그룹핑)·`view`(리스트/간트)는 응답에 포함될 데이터 자체를 바꾸지 않는 화면 전용 상태이므로 쿼리 파라미터로 보내지 않는다 — URL에는 남지만(URL 우선 원칙) 서버 계약과는 무관하다.

### `POST /api/projects/{projectKey}/tasks`
목업 함수 대응 없음 (2026-08-29, 백엔드 구현 중 추가 — 기존엔 `POST /tasks/{parentId}/subtasks`로 하위업무만 만들 수 있고 최상위 업무를 만드는 방법이 없었다). 최상위 업무 생성용.

**Request**
```ts
{
  title: string             // 필수
  status?: TaskStatus       // 기본값 'todo'
  priority?: TaskPriority   // 기본값 'medium'
  startDate?: string        // 기본값: endDate가 있으면 그 값, 없으면 오늘
  endDate?: string          // 기본값: startDate와 동일
  assigneeIds?: string[]    // 기본값 []. 프로젝트 멤버가 아닌 userId가 섞이면 400
  tagIds?: string[]         // 기본값 []. 프로젝트에 속하지 않은 tagId가 섞이면 400
  isPrivate?: boolean       // 기본값 false
}
```
**Response** `201` → [`Task`](#task)

**비고**: `progress`는 항상 `0`으로 시작한다(요청으로 지정 불가). `code`는 서버가 프로젝트 접두어(`projectKey`)에 그 프로젝트 내 최대 순번 + 1을 붙여 자동 발급한다 (하위업무 생성과 동일 규칙, 아래 참고). `assigneeIds`를 지정하면 각 대상에게 `task_assigned` 알림이 생성된다 (7장 트리거와 동일).

### `GET /api/me/project-stats`
목업 함수 대응 없음 (목업의 `fetchAllTasks(): Promise<Task[]>`을 대체하는 집계 엔드포인트).
전체 프로젝트 홈의 카드별 진행 현황 계산용 — 요청자가 접근 가능한 **모든 프로젝트**의 업무를 프로젝트별로 집계해 반환한다. `/me` 네임스페이스를 쓰는 이유: `/api/projects/{projectKey}` 단건 조회 경로와 겹치지 않도록, 그리고 "요청자 기준" 집계임을 경로에서 드러내기 위해.

**Response** `200`
```ts
{ projectId: string; total: number; todo: number; progress: number; review: number; done: number }[]
```

> 목업 함수는 전체 프로젝트의 업무 원본을 통째로 내려받아 프론트에서 `statsOf(project)`로 집계한다. 프로젝트가 50~100개 규모면 매 홈 화면 진입마다 불필요하게 큰 페이로드를 전송하게 되므로 집계된 숫자만 반환하는 이 엔드포인트로 대체한다 → 10장 참고. 업무 원문이 필요한 화면(업무 리스트·상세)은 위 `GET /api/projects/{projectKey}/tasks`, `GET /api/tasks/{taskId}`로 별도 조회한다.

> **권한 (2026-08-29 확정)**: 이 엔드포인트는 tasks 메뉴 권한으로 게이팅하지 않는다 — 전체 프로젝트 홈 카드는 "업무" 화면 진입권과 무관하게 보여야 하기 때문. 프로젝트 멤버십(0.4절)만 확인하고, isPrivate 업무는 여전히 집계에서 제외한다(담당자·참여자 제외).

### `GET /api/me/task-count`
목업 함수: `fetchMyTaskCount(): Promise<number>`
사이드바 "내 업무" 배지. `assigneeIds`에 내가 포함되고 `status !== 'done'`인 업무 수.

**Response** `200` → `number`

### `GET /api/tasks/{taskId}`
목업 함수: `fetchTaskById(taskId): Promise<Task | undefined>`

**Response** `200` → [`Task`](#task) / `404`

### `GET /api/tasks/{taskId}/subtask-count`
목업 함수: `fetchSubtaskCount(taskId): Promise<number>` — `parentId === taskId`인 업무 수.

**Response** `200` → `number`

### `POST /api/tasks/{parentId}/subtasks`
목업 함수: `createSubtask(parentId, title): Promise<Task>`

**Request** `{ title: string }`

**Response** `201` → [`Task`](#task)

**비고**: 서버가 `code`를 자동 발급한다 — 부모의 프로젝트 접두어(`code.split('-')[0]`)에 그 프로젝트 내 최대 순번 + 1을 붙인다 (예: `APP-142`의 하위업무 → `APP-201`처럼 프로젝트 전체에서 다음 순번). `status: 'todo'`, `priority: 'medium'`, `progress: 0`, `isPrivate: false`, `parentId`는 요청받은 부모 ID, `startDate`/`endDate`는 부모와 동일하게 초기화.

### `PATCH /api/tasks/{taskId}/status`
목업 함수: `updateTaskStatus(taskId, status): Promise<Task | undefined>`

**Request**
```ts
{ status: TaskStatus }  // 'todo' | 'progress' | 'review' | 'done'
```
**Response** `200` → [`Task`](#task) / `404`

**비고**: `status === 'done'`이면 서버가 `progress`를 자동으로 `100`으로 맞춘다.

### `PATCH /api/tasks/{taskId}`
목업 함수: `updateTask(taskId, patch): Promise<Task | undefined>`

**Request** (부분 수정, 아래 필드만 허용)
```ts
type TaskPatch = Partial<
  Pick<Task, 'title' | 'priority' | 'startDate' | 'endDate' | 'progress' | 'isPrivate'>
>
```
**Response** `200` → [`Task`](#task) / `404`

### `PATCH /api/tasks/{taskId}/assignees`
목업 함수: `updateTaskAssignees(taskId, assigneeIds): Promise<Task | undefined>`
담당자 전체 목록을 교체한다 (부분 추가/삭제 아님).

**Request** `{ assigneeIds: string[] }`
**Response** `200` → [`Task`](#task) / `404`

**비고**: 새로 추가된 담당자에게 `task_assigned` 알림 생성 (7장 참고).

### `PATCH /api/tasks/{taskId}/dependencies`
목업 함수: `updateTaskDependencies(taskId, dependencyIds): Promise<Task | undefined>`
간트차트 선행 업무 목록 전체 교체.

**Request** `{ dependencyIds: string[] }`
**Response** `200` → [`Task`](#task) / `404`

### `GET /api/projects/{projectKey}/tags`
목업 함수: `fetchTagsByProjectKey(projectKey): Promise<Tag[]>`

**Response** `200` → [`Tag`](#tag)`[]`

---

## 5. 댓글

### `GET /api/tasks/{taskId}/comments`
목업 함수: `fetchCommentsByTaskId(taskId): Promise<Comment[]>`
`createdAt` 오름차순 정렬해 반환.

**Response** `200` → [`Comment`](#comment)`[]`

### `POST /api/tasks/{taskId}/comments`
목업 함수: `addComment(taskId, body, mentionUserIds): Promise<Comment>`

**Request**
```ts
{ body: string; mentionUserIds: string[] }
```
**Response** `201` → [`Comment`](#comment)

**비고**: 서버는 댓글 생성과 함께
1. `task.commentCount`를 1 증가시키고,
2. `mentionUserIds`에 포함된 사용자에게 `task_mention` 알림을,
3. 그 외 해당 업무의 담당자·참여자(작성자 본인 제외)에게 `task_comment` 알림을
생성한다.

---

## 6. 메신저

> **채널 생성 엔드포인트 (2026-08-29 추가)**
> 목업 함수 대응 없음 — 목업 데이터가 이미 채널이 있는 상태로 시작해서 "채널 생성" 화면/버튼이 없었다.
> 원래 API-SPEC.md 6장에는 채널을 만드는 방법 자체가 없었는데, 그러면 메신저 기능을 실제로 테스트할
> 방법이 없어 6단계 최상위 업무 생성 신설과 같은 이유로 사용자와 합의해 아래 두 엔드포인트를 신설했다.
> 응답이 `Channel` 하나뿐인 목업 함수가 없으므로 `GET /api/projects/{projectKey}/channels`로
> 새로고침하거나, 이 엔드포인트의 응답을 그대로 로컬 목록에 추가해서 쓰면 된다.

### `GET /api/projects/{projectKey}/channels`
목업 함수: `fetchChannelsByProjectKey(projectKey): Promise<Channel[]>`
각 채널의 `unreadCount`는 요청자 기준 (채널의 `last_read_at` 이후 메시지 수).

**Response** `200` → [`Channel`](#channel)`[]`

### `POST /api/projects/{projectKey}/channels`
목업 함수 대응 없음 (2026-08-29 신설). 그룹 채널 생성용.

**Request**
```ts
{ name: string; memberIds?: string[] }   // 요청자는 memberIds에 없어도 항상 자동 포함된다
```
**Response** `201` → [`Channel`](#channel)

**비고**: `memberIds`가 이 프로젝트 멤버가 아닌 userId를 포함하면 **400**.

### `POST /api/projects/{projectKey}/channels/dm`
목업 함수 대응 없음 (2026-08-29 신설). 1:1 DM 채널 생성 — **멱등**: 요청자·`targetUserId` 두 사람으로
이루어진 DM 채널이 이미 있으면 새로 만들지 않고 그 채널을 그대로 반환한다.

**Request**
```ts
{ targetUserId: string }
```
**Response** `200` → [`Channel`](#channel)  (신규 생성이든 기존 채널 반환이든 항상 200 — 멱등 동작이라 클라이언트가 신규/기존을 구분할 필요가 없다)

**비고**: `targetUserId`가 이 프로젝트 멤버가 아니면 **400**. `targetUserId`가 요청자 자신이면 **400**.

### `GET /api/projects/{projectKey}/channels/unread-count`
목업 함수: `fetchUnreadChannelCount(projectKey): Promise<number>`
컨텍스트 바 "메신저" 탭 배지 — 프로젝트 내 모든 채널의 안읽음 합계.

**Response** `200` → `number`

### `GET /api/channels/{channelId}/messages`
목업 함수: `fetchMessagesByChannelId(channelId): Promise<Message[]>`

**Response** `200` → [`Message`](#message)`[]`

> `DEN-DESIGN.md` 6.1절에는 `?before=&limit=` 커서 페이지네이션이 예약되어 있다. 목업은 채널의 전체 메시지를 반환한다 → 10장 참고.

### `POST /api/channels/{channelId}/messages`
목업 함수: `sendMessage(channelId, body, mentionUserIds): Promise<Message>`

**Request**
```ts
{ body: string; mentionUserIds: string[] }
```
**Response** `201` → [`Message`](#message)

**비고**: 저장과 동시에 STOMP `/topic/channel/{channelId}`로 브로드캐스트한다 (8장). 채널을 지금 보고 있지 않은 멤버에게는 `channel_message` 알림도 생성한다 — "지금 보고 있음"은 그 채널 토픽(`/topic/channel/{channelId}`)을 STOMP로 구독 중인지로 판단하며(Redis에 구독 상태 기록), 작성자 본인은 항상 제외한다. `mentionUserIds`가 채널 멤버가 아닌 userId를 포함하면 **400**.

### `POST /api/channels/{channelId}/read`
목업 함수: `markChannelRead(channelId): Promise<void>`
요청자의 `last_read_at`을 현재 시각으로 갱신 (해당 채널 안읽음 수 0으로).

**Response** `204`

---

## 7. 알림

### `GET /api/notifications`
목업 함수: `fetchNotifications(): Promise<AppNotification[]>`
요청자 본인의 알림 전체, 최신순.

**Response** `200` → [`AppNotification`](#appnotification)`[]`

### `GET /api/notifications/unread-count`
목업 함수: `fetchUnreadNotificationCount(): Promise<number>`

**Response** `200` → `number`

### `POST /api/notifications/{notificationId}/read`
목업 함수: `markNotificationRead(notificationId): Promise<void>`

**Response** `204`

### `POST /api/notifications/read-all`
목업 함수: `markAllNotificationsRead(): Promise<void>`
요청자 본인의 안읽음 알림 전체를 읽음 처리.

**Response** `204`

**알림 생성 트리거 정리** (서버가 상황별로 `AppNotification`을 만들어야 하는 지점):

| `NotificationType` | 생성 시점 |
|---|---|
| `task_assigned` | `PATCH /tasks/{id}/assignees`로 새로 담당자에 추가됐을 때 |
| `task_comment` | 댓글 작성 시, 멘션 대상 제외한 담당자·참여자에게 |
| `task_mention` | 댓글 본문에서 멘션됐을 때 |
| `task_due_soon` | `endDate`가 임박(당일/1일 전)했는데 `status !== 'done'`인 업무 — 배치/스케줄러가 생성 |
| `task_status_changed` | `PATCH /tasks/{id}/status`로 상태가 바뀌었을 때, 담당자·참여자에게 |
| `channel_message` | 채널 메시지 발송 시, 그 채널을 보고 있지 않은 멤버에게 |
| `project_invited` | `POST /projects/{projectKey}/members`로 초대됐을 때 |

> `task_due_soon`은 주기가 스펙에 없어 2026-08-29 사용자와 합의: 매일 09:00 1회(`TaskDueSoonScheduler`) 스캔하고,
> 같은 업무·수신자 조합은 당일 이미 알림이 있으면 다시 만들지 않는다.
> `channel_message`는 8단계(메신저)에서 메시지 발송 API와 함께 구현했다 — "보고 있지 않은 멤버" 판단 방식은
> 6장 `POST /channels/{channelId}/messages` 비고 참고.
>
> 모든 알림은 생성과 동시에 STOMP `/user/queue/notifications`로도 실시간 푸시된다 (8장 WebSocket 참고,
> 8단계에서 배선). REST 조회·읽음 처리 API는 실시간 연결이 끊겨 있던 동안 놓친 알림을 따라잡기 위한 것이다.

---

## 8. WebSocket (STOMP) — 참고

REST가 아니지만 메신저·알림 실시간성에 필요한 계약. 상세는 `DEN-DESIGN.md` 6.2절.

```
연결   /ws                              JWT 핸드셰이크 인증
구독   /topic/channel/{channelId}       채널 새 메시지 → Message
구독   /user/queue/notifications        개인 알림 실시간 → AppNotification
발행   /app/channel/{channelId}/send    메시지 전송 (REST POST 대신 또는 함께 사용 가능)
발행   /app/channel/{channelId}/typing  입력 중 표시
```

> **구현 세부사항 (2026-08-29, 8단계에서 확정 — DEN-DESIGN.md 6.2절엔 이 정도 세부사항까지는 없었음)**
> - **`/ws` 인증**: 브라우저 네이티브 WebSocket API는 핸드셰이크 요청에 커스텀 헤더를 못 실으므로,
>   `Authorization` 헤더 대신 쿼리 파라미터로 액세스 토큰을 전달한다 — `/ws?token=<accessToken>`.
> - **타이핑 구독 목적지**: 원 설계엔 발행(`/app/.../typing`) 목적지만 있고 구독 목적지가 없었다.
>   메시지 토픽(`/topic/channel/{channelId}`)은 `Message` 타입 계약을 지켜야 해서 섞지 않고,
>   별도로 `/topic/channel/{channelId}/typing`을 구독하도록 정했다. 페이로드: `{ userId, at }`.

---

## 9. 타입 정의 (요청·응답에서 참조)

`src/mock/types.ts`와 동일하다. 필드 의미가 자명하지 않은 것만 주석으로 부연했다.

```ts
type TaskStatus = 'todo' | 'progress' | 'review' | 'done'
type TaskPriority = 'urgent' | 'high' | 'medium' | 'low'
type MenuKey = 'tasks' | 'gantt' | 'messenger'
type NotificationType =
  | 'task_mention'
  | 'task_assigned'
  | 'task_comment'
  | 'task_due_soon'
  | 'task_status_changed'
  | 'channel_message'
  | 'project_invited'
```

#### User
```ts
interface User {
  id: string
  name: string
  email: string
  initials: string
  avatarGradient: string   // 예: 'linear-gradient(135deg,#f59e0b,#ef4444)'
  title?: string           // 예: '프로젝트 리드'
}
```

#### Project
```ts
interface Project {
  id: string
  key: string              // 사람이 읽는 키 ("APP"). URL에 노출됨
  name: string
  description: string
  color: string             // 사이드바 점 색상 / 카드 마크 배경
  folderId: string | null   // null이면 미분류 (요청자 개인 배치 기준)
  memberIds: string[]
}
```

#### Folder
```ts
interface Folder {
  id: string
  name: string
  collapsed?: boolean
}
```

#### Role
```ts
interface Role {
  id: string
  projectId: string
  name: string
  isAdmin: boolean
  menuPermissions: Record<MenuKey, boolean>
}
```

#### ProjectMember
```ts
interface ProjectMember {
  userId: string
  projectId: string
  roleId: string
  invitedAt: string   // ISO date
}
```

#### Tag
```ts
interface Tag {
  id: string
  projectId: string
  name: string
}
```

#### Task
```ts
interface Task {
  id: string
  code: string              // 예: "APP-142"
  projectId: string
  title: string
  status: TaskStatus
  priority: TaskPriority
  assigneeIds: string[]     // 다대다
  watcherIds: string[]
  parentId: string | null
  dependencyIds: string[]   // 선행 업무 (간트용)
  tagIds: string[]
  startDate: string         // ISO date
  endDate: string           // ISO date
  progress: number          // 0-100
  isPrivate: boolean
  commentCount: number
}
```

#### Comment
```ts
interface Comment {
  id: string
  taskId: string
  authorId: string
  body: string
  mentionUserIds: string[]
  createdAt: string   // ISO datetime
}
```

#### Channel
```ts
interface Channel {
  id: string
  projectId: string
  name: string
  type: 'group' | 'dm'
  memberIds: string[]
  unreadCount: number   // 요청자 기준
}
```

#### Message
```ts
interface Message {
  id: string
  channelId: string
  authorId: string
  body: string
  mentionUserIds: string[]
  createdAt: string   // ISO datetime
}
```

#### AppNotification
```ts
interface AppNotification {
  id: string
  userId: string
  type: NotificationType
  title: string
  body: string
  projectKey: string | null
  linkTaskId?: string      // 있으면 업무 상세로 이동
  linkChannelId?: string   // 있으면 해당 채널로 이동
  isRead: boolean
  createdAt: string   // ISO datetime
}
```

---

## 10. 백엔드 전환 시 프론트가 함께 고쳐야 할 것

목업 함수를 실제 axios 호출로 바꾸는 것만으로 끝나지 않고, **호출부의 동작 자체를 같이
고쳐야 하는 항목**만 모았다. 각 장에는 "→ 10장 참고"로만 짧게 표시해뒀으니, 백엔드
연동 작업을 시작할 때 이 표를 체크리스트로 쓴다.

| 영역 | 목업(현재) | 백엔드 전환 후 | 관련 엔드포인트 | 관련 프론트 코드 |
|---|---|---|---|---|
| 업무 목록 | 프로젝트의 전체 업무를 받아 `route.query` 기준으로 필터·검색·페이지네이션을 **클라이언트에서** 계산 | 쿼리 파라미터(`status`/`assignee`/`priority`/`tag`/`q`/`page`/`size`)로 **서버에 위임**, 응답을 `{ items, total, page, size }`로 소비 | 4장 `GET /projects/{projectKey}/tasks` | `TaskListPage.vue`의 필터링·페이지네이션 로직 |
| 즐겨찾기 | 토글 응답으로 **갱신된 즐겨찾기 전체 목록**(`string[]`)을 받아 그대로 치환 | 토글 응답은 `{ isFavorite }` 단건뿐 — 로컬 목록에서 직접 추가/제거하거나 `GET /api/favorites` 캐시를 무효화 | 2장 `POST /projects/{projectKey}/favorite` | 즐겨찾기 토글을 호출하는 화면들(`ProjectsHomePage.vue`, `Sidebar.vue` 등) |
| 멤버 초대 | 이름·이메일을 입력받아 그 자리에서 **신규 계정 생성**, 응답으로 생성된 `User`를 받아 멤버 목록에 반영 | 계정은 생성하지 않음 — `GET /api/users?q=`로 **기존 사용자를 검색·선택**해 `userId`를 전달. **응답 타입도 `User` → `ProjectMember`로 바뀜** — 초대 후 목록 갱신 로직이 응답에서 `User` 필드(`name`/`email`/`avatarGradient` 등)를 직접 쓰고 있다면, `ProjectMember`(`userId`/`roleId`/`invitedAt`)로는 바로 대체가 안 되므로 이미 알고 있는 선택된 `User` 객체와 조합하거나 멤버 목록을 다시 조회(invalidate)하도록 고쳐야 한다 | 3장 `POST /projects/{projectKey}/members`, `GET /api/users?q=` | `MembersSettingsPage.vue`의 초대 폼 + 초대 성공 후 목록 갱신 로직 |
| 멘션·담당자 선택 | `fetchUsers()`로 **전체 사용자** 목록을 받아 멘션·담당자 후보로 그대로 사용 | 프로젝트 멤버로 범위가 좁아진 `GET /api/projects/{projectKey}/users`로 교체 | 3장 `GET /api/projects/{projectKey}/users` | 담당자 피커, 댓글·메시지 멘션 입력 컴포넌트 |
| 전체 프로젝트 홈 통계 | `fetchAllTasks()`로 **전체 프로젝트의 업무 원본**을 받아 카드별로 `statsOf()` 클라이언트 집계 | 집계된 숫자만 받는 `GET /api/me/project-stats`로 교체 — 클라이언트 집계 로직 제거 | 4장 `GET /api/me/project-stats` | `ProjectsHomePage.vue`의 `fetchAllTasks`/`statsOf()` |
| 메신저 메시지 | 채널의 **전체 메시지**를 한 번에 로드 | `?before=&limit=` 커서 기반 페이지네이션 + 무한 스크롤로 변경 (추후 — 메시지 양이 실제로 문제될 때 진행) | 6장 `GET /channels/{channelId}/messages` | `MessengerPage.vue`, `ChatPanel.vue` |
| 최상위 업무 생성 | 목업에는 대응 함수 자체가 없음 — 목업 데이터가 이미 시드된 상태로 시작해서 "업무 생성" 화면/버튼이 없었다 | `POST /api/projects/{projectKey}/tasks` 신설 (2026-08-29). **프론트도 목업에 없던 이 API를 호출하도록 새로 추가해야 한다** — 업무 생성 UI(버튼·폼)가 아직 없다면 이 화면부터 만들어야 함 | 4장 `POST /api/projects/{projectKey}/tasks` | (신규) 업무 목록/보드 화면에 업무 생성 진입점 추가 필요 |
| 채널·DM 생성 | 목업에는 대응 함수 자체가 없음 — 목업 데이터가 이미 채널이 있는 상태로 시작해서 "채널 생성" 화면/버튼이 없었다 | `POST /api/projects/{projectKey}/channels`(그룹), `POST /api/projects/{projectKey}/channels/dm`(1:1) 신설 (2026-08-29). **프론트에 채널 생성 UI가 없으므로 새로 만들어야 함** — 그룹 채널 만들기 버튼(이름·멤버 선택)과 DM 시작하기(멤버 목록에서 상대 선택 → `POST .../dm` 호출, 응답 채널로 바로 이동) 둘 다 필요 | 6장 `POST /api/projects/{projectKey}/channels`, `POST /api/projects/{projectKey}/channels/dm` | (신규) `MessengerPage.vue`에 채널 생성·DM 시작 진입점 추가 필요 |
