-- db/seed.sql
-- 로컬 개발용 초기 데이터(테스트 계정 등).
--
-- docker-compose 초기화 시 자동 실행되는 init.sql과 달리 이 파일은 수동으로 실행한다.
-- 이유: users 테이블은 애플리케이션이 최초 기동해 ddl-auto: update로 스키마를 만든 뒤에야
-- 존재하므로, 컨테이너 초기화 시점(docker-entrypoint-initdb.d)에는 아직 테이블이 없어 실행할 수 없다.
--
-- 실행 순서:
--   1. docker compose up -d           (init.sql이 자동 실행되어 DB/계정 생성)
--   2. ./gradlew bootRun 한 번 띄워서 종료 (ddl-auto: update로 테이블 생성)
--   3. psql -h localhost -U pulse_user -d pulsedb -f db/seed.sql
--
-- 비밀번호는 bcrypt(strength 10, BCryptPasswordEncoder 기본값 — SecurityConfig와 동일)로
-- 미리 해시해 넣었다. 평문 비밀번호는 test1234.
-- 이메일에 UNIQUE 제약이 있으므로 재실행해도 안전하게 스킵된다(ON CONFLICT DO NOTHING).

INSERT INTO users (id, email, password_hash, name, initials, avatar_gradient, title, created_at, updated_at)
VALUES (
    'ba9c5576-99dd-437e-9116-2c389abc234e',
    'jk.jung@pleanb.com',
    '$2a$10$edt/N6o0.FS4TE6v7DsLQeihJdp.6ZijnMy6hJGUpal6S9/WmeLrO', -- test1234
    '정경호',
    'JK',
    'linear-gradient(135deg,#4f46e5,#818cf8)',
    NULL,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, email, password_hash, name, initials, avatar_gradient, title, created_at, updated_at)
VALUES (
    '1fb8dc26-cc9e-4b7a-b21a-06d716fc6dc0',
    'yj.shin@pleanb.com',
    '$2a$10$eWA9XqMRu9mCr5ko/66Kv.D4kIA0HKcXHeK4FcmtGQLBIuHJncMDO', -- test1234
    '신유진',
    'YJ',
    'linear-gradient(135deg,#f59e0b,#fbbf24)',
    NULL,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, email, password_hash, name, initials, avatar_gradient, title, created_at, updated_at)
VALUES (
    'bc91929c-e394-4afc-896b-a4edd8b7eab4',
    'ms.kim@pleanb.com',
    '$2a$10$aLW6prMyNAchdxRLX0x4..7SFmBBYcCNI6okzJWtbgRtZG61hJc6y', -- test1234
    '김민석',
    'MS',
    'linear-gradient(135deg,#10b981,#34d399)',
    NULL,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, email, password_hash, name, initials, avatar_gradient, title, created_at, updated_at)
VALUES (
    '634fc2cb-7534-4c4a-a993-7e206a076f45',
    'sr.han@pleanb.com',
    '$2a$10$yqGkQB.gk4sjSe0pcyLTouUyDEraF2HbxWTJqNx6Iei2SACZOwG5.', -- test1234
    '한소라',
    'SR',
    'linear-gradient(135deg,#ec4899,#f472b6)',
    NULL,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;
