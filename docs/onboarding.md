# 개발 환경 세팅 가이드

## 필요한 것
- Docker Desktop 설치 및 실행 중일 것

## 세팅 순서

1. 저장소 clone
   ```bash
   git clone https://github.com/gayo73/Stock-System.git
   cd Stock-System/docker
   ```

2. 환경변수 파일 생성
   ```bash
   cp .env.example .env
   ```
   `.env` 안의 값은 팀 공지된 값 그대로 사용

3. 컨테이너 실행
   ```bash
   docker compose up -d
   ```

4. 상태 확인
   ```bash
   docker compose ps
   ```
   모두 `Up`이면 정상

## 접속 정보

| 서비스 | 주소 |
|---|---|
| MySQL | localhost:3307 |
| Redis | localhost:6379 |
| Kafka | localhost:9092 |
| Kafka UI | http://localhost:8090 |
| Redis Commander | http://localhost:8091 |

## 자주 쓰는 명령어

```bash
docker compose down       # 중지 (데이터 유지)
docker compose down -v    # 중지 + 데이터 완전 삭제 (스키마 바뀌었을 때)
docker compose logs -f 서비스명   # 로그 실시간 확인
```

## Windows / Git Bash 사용자 참고사항

- `docker exec -it`가 들어간 명령어는 앞에 `winpty`를 붙이세요:
```bash
  winpty docker exec -it securities-mysql mysql -u user -p securities_db
```
- 컨테이너 안 파일 경로(`/opt/...`)가 들어간 명령어는 Windows 경로로 잘못 변환될 수 있습니다. 이 경우:
```bash
  winpty docker exec -it 컨테이너명 sh
```
- 컨테이너 안에 먼저 들어간 다음, 그 안에서 명령어를 실행하세요.


## 문제 생기면

1. `docker compose ps`로 어떤 서비스가 죽었는지 확인
2. `docker compose logs 서비스명`으로 에러 로그 확인
3. 그래도 안 되면 팀 채널에 로그 캡처해서 공유