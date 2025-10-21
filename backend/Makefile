.PHONY: up down build restart logs clean

# 컨테이너 시작
up:
	docker compose up -d

# 빌드 후 시작
build:
	docker compose up --build -d

# 컨테이너 종료
down:
	docker compose down

# 재시작
restart:
	docker compose restart

# 로그 확인
logs:
	docker compose logs -f

# 서버 로그만 확인
logs-server:
	docker logs -f server

# DB 로그만 확인
logs-db:
	docker logs -f mysql

# 완전 삭제 (볼륨 포함)
clean:
	docker compose down -v

# 상태 확인
ps:
	docker compose ps