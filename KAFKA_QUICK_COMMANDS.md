# Kafka 메시지 확인 명령어 (바로 사용 가능)

## Windows PowerShell

### 메시지 확인 (가장 간단한 방법)
```powershell
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning
```

### Topic 목록 확인
```powershell
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
```

### Topic 상세 정보 확인
```powershell
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-topics.sh --describe --topic order-created --zookeeper zookeeper:2181
```

### 최신 메시지만 확인 (--from-beginning 제거)
```powershell
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created
```

### 컨테이너 상태 확인

#### 실행 중인 컨테이너만 확인
```powershell
docker ps | findstr kafka
```

#### 모든 컨테이너 확인 (중지된 것 포함)
```powershell
docker ps -a | findstr kafka
```

#### docker-compose로 상태 확인 (권장)
```powershell
docker-compose -f src/main/resources/docker-compose.yml ps
```

#### 컨테이너 상태 확인 명령어 설명
- `docker ps`: 실행 중인 컨테이너만 표시
- `docker ps -a`: 모든 컨테이너 표시 (중지된 것 포함)
- `Status` 컬럼 확인:
  - `Up X minutes`: 실행 중 ✅
  - `Exited (0) X minutes ago`: 중지됨 ⏹️
  - `Created`: 생성되었지만 시작되지 않음

## Linux/Mac (bash)

### 메시지 확인 (가장 간단한 방법)
```bash
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning
```

### Topic 목록 확인
```bash
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
```

### Topic 상세 정보 확인
```bash
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-topics.sh --describe --topic order-created --zookeeper zookeeper:2181
```

### 최신 메시지만 확인 (--from-beginning 제거)
```bash
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created
```

### 컨테이너 상태 확인

#### 실행 중인 컨테이너만 확인
```bash
docker ps | grep kafka
```

#### 모든 컨테이너 확인 (중지된 것 포함)
```bash
docker ps -a | grep kafka
```

#### docker-compose로 상태 확인 (권장)
```bash
docker-compose -f src/main/resources/docker-compose.yml ps
```

#### 컨테이너 상태 확인 명령어 설명
- `docker ps`: 실행 중인 컨테이너만 표시
- `docker ps -a`: 모든 컨테이너 표시 (중지된 것 포함)
- `Status` 컬럼 확인:
  - `Up X minutes`: 실행 중
  - `Exited (0) X minutes ago`: 중지됨
  - `Created`: 생성되었지만 시작되지 않음

## Kafka 시작/종료 명령어

### Windows PowerShell
```powershell
# 시작
docker-compose -f src/main/resources/docker-compose.yml up -d

# 종료
docker-compose -f src/main/resources/docker-compose.yml down

# 종료 및 볼륨 삭제
docker-compose -f src/main/resources/docker-compose.yml down -v
```

### Linux/Mac (bash)
```bash
# 시작 다시 시작
docker-compose -f src/main/resources/docker-compose.yml up -d

# 종료(컨테이너와 네트워크 삭제제)
docker-compose -f src/main/resources/docker-compose.yml down

# 컨테이너만 중지
docker-compose -f src/main/resources/docker-compose.yml stop

# 종료 및 볼륨 삭제(데이터 까지 삭제)
docker-compose -f src/main/resources/docker-compose.yml down -v
```

