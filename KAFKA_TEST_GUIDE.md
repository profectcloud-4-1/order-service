# Kafka 통신 확인 방법

## 1. Kafka 서버 실행 확인

### Docker Compose로 Kafka 실행
```bash
docker-compose -f src/main/resources/docker-compose.yml up -d
```

### Kafka 상태 확인

#### Windows PowerShell
```powershell
# Kafka 컨테이너가 실행 중인지 확인
docker ps | Select-String kafka

# 또는 findstr 사용
docker ps | findstr kafka

# Kafka 컨테이너 ID 찾기
$kafkaContainerId = (docker ps | Select-String kafka).Line.Split()[0]

# Topic이 생성되었는지 확인 (Kafka 컨테이너 내부에서)
docker exec -it $kafkaContainerId /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
```

#### Linux/Mac (bash)
```bash
# Kafka 컨테이너가 실행 중인지 확인
docker ps | grep kafka

# Topic이 생성되었는지 확인 (Kafka 컨테이너 내부에서)
docker exec -it <kafka-container-id> /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
```

## 2. 애플리케이션 실행 및 로그 확인

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### 로그에서 확인할 내용
주문 생성 API 호출 시 다음 로그가 출력되어야 합니다:

```
INFO  - 주문 생성 시작: userId=..., itemCount=...
INFO  - 주문 엔티티 저장 완료: orderId=..., status=PENDING
INFO  - Kafka 재고 차감 요청 발행: topic=order-created, orderId=..., productCount=...
INFO  - Kafka 재고 차감 요청 발행 성공: orderId=..., offset=...
INFO  - 재고 차감 요청 발행 완료 (비동기): orderId=..., productCount=...
```

## 3. Kafka 메시지 확인 (수동 테스트)

### Kafka Consumer로 메시지 확인

#### Windows PowerShell (바로 사용 가능)
```powershell
# 방법 1: docker-compose 서비스 이름으로 바로 사용 (권장)
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning

# 방법 2: 컨테이너 ID 자동 찾기
$kafkaContainerId = (docker ps --format "{{.ID}}" | Select-String -Pattern ".*" | Select-Object -First 1 | ForEach-Object { docker ps --format "{{.ID}}\t{{.Names}}" | Select-String kafka }).Line.Split("`t")[0]
docker exec -it $kafkaContainerId /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning

# 방법 3: 컨테이너 이름으로 직접 사용 (docker-compose 기본 네이밍)
docker exec -it order-service-1-kafka-1 /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning
```

#### Linux/Mac (bash)
```bash
# 방법 1: docker-compose 서비스 이름으로 바로 사용 (권장)
docker-compose -f src/main/resources/docker-compose.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning

# 방법 2: 컨테이너 ID로 사용
docker exec -it <kafka-container-id> /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --from-beginning
```

### 메시지 형식 확인
메시지는 다음과 같은 JSON 형식으로 전송됩니다:
```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "products": [
    {
      "productId": "123e4567-e89b-12d3-a456-426614174001",
      "quantity": 2
    }
  ]
}
```

## 4. 통신 실패 시 확인사항

### 1. Kafka 연결 확인
- `application.yml`의 `bootstrap-servers: localhost:9092` 확인
- Kafka 컨테이너가 실행 중인지 확인

### 2. Topic 생성 확인
- `docker-compose.yml`의 `KAFKA_CREATE_TOPICS` 설정 확인
- Topic이 실제로 생성되었는지 확인

### 3. 직렬화 설정 확인
- `KafkaConfig.java`에서 `JsonSerializer` 사용 확인
- `application.yml`의 `value-serializer` 설정 확인

### 4. 로그 확인
- Kafka Producer 발행 실패 로그 확인
- 예외 메시지 확인

## 5. 테스트 방법

### API 호출로 테스트

#### Windows PowerShell
```powershell
# 주문 생성 API 호출
$body = @{
    items = @(
        @{
            productId = "123e4567-e89b-12d3-a456-426614174001"
            quantity = 2
        }
    )
    totalAmount = 10000
    orderName = "테스트 주문"
    address = "서울시 강남구"
    addressDetail = "테헤란로 123"
    zipcode = "06142"
    phone = "010-1234-5678"
    name = "홍길동"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/orders" `
    -Method POST `
    -ContentType "application/json" `
    -Headers @{Authorization = "Bearer <token>"} `
    -Body $body
```

#### Linux/Mac (bash) 또는 curl 사용
```bash
# 주문 생성 API 호출
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "items": [
      {
        "productId": "123e4567-e89b-12d3-a456-426614174001",
        "quantity": 2
      }
    ],
    "totalAmount": 10000,
    "orderName": "테스트 주문",
    "address": "서울시 강남구",
    "addressDetail": "테헤란로 123",
    "zipcode": "06142",
    "phone": "010-1234-5678",
    "name": "홍길동"
  }'
```

호출 후 Kafka Consumer로 메시지가 전송되었는지 확인하세요.

