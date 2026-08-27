-- db/init.sql
-- 로컬 Docker Postgres 컨테이너를 "최초" 기동할 때 docker-compose가
-- /docker-entrypoint-initdb.d 에 마운트해 자동 실행하는 스크립트.
--
-- 주의:
-- - postgres 공식 이미지는 데이터 디렉터리(볼륨)가 "비어 있을 때"만 이 폴더의 스크립트를 실행한다.
--   이미 초기화된 볼륨을 재사용 중이면 이 파일을 고쳐도 반영되지 않는다.
--   다시 실행하려면 컨테이너를 내리고 볼륨을 삭제한 뒤 재기동해야 한다.
--   (docker compose down -v)
-- - 여기 있는 계정·비밀번호는 로컬 개발 전용 기본값이다. 운영 환경에 그대로 쓰지 말 것.
--   개인적으로 다른 값을 쓰고 싶으면 이 파일과 application-local.yaml을 함께 맞춰 바꾸면 된다.
-- - 테이블 스키마는 여기서 만들지 않는다. 애플리케이션이 최초 기동할 때
--   spring.jpa.hibernate.ddl-auto: update 로 자동 생성한다 (CLAUDE.md 참고).
--   따라서 seed.sql(테스트 데이터)은 앱을 한 번 띄워 테이블이 생긴 뒤에 수동으로 실행해야 한다.

CREATE ROLE pulse_user WITH LOGIN PASSWORD 'CHANGE_ME';

CREATE DATABASE pulsedb OWNER pulse_user;

GRANT ALL PRIVILEGES ON DATABASE pulsedb TO pulse_user;

\connect pulsedb

GRANT ALL ON SCHEMA public TO pulse_user;
