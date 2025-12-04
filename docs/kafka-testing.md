# Kafka 메시지 발행 확인 방법

## 방법 1: KafkaUI 사용 (권장)

### 1. Kafka + KafkaUI 실행

```bash
# Docker Compose로 Kafka와 KafkaUI 실행
docker-compose -f docker-compose-kafka-ui.yml up -d

# 실행 확인
docker ps | grep -E "kafka|zookeeper|kafka-ui"
```

### 2. KafkaUI 접속

브라우저에서 `http://localhost:8080` 접속

### 3. 테스트 실행 후 확인

```bash
# 테스트 실행
./gradlew test --tests OrderStockIntegrationTest

# KafkaUI에서 확인:
# 1. Topics 메뉴 클릭
# 2. "stock-service-topic" 선택
# 3. Messages 탭에서 발행된 메시지 확인
```

### 4. 실시간 모니터링

KafkaUI에서 메시지를 실시간으로 확인하면서 애플리케이션을 실행할 수 있습니다.

---

## 방법 2: 테스트 로그 확인

테스트 실행 시 콘솔에 메시지 내용이 출력됩니다:

```bash
./gradlew test --tests OrderStockIntegrationTest --info
```

출력 예시:
```
=== Kafka 메시지 발행 확인 ===
Topic: stock-service-topic
Partition: 0
Offset: 0
Message: {"orderId":"...","items":[...],"occurredAt":"..."}
================================
```

---

## 방법 3: 실제 Kafka 브로커 사용 (통합 테스트)

### 설정

`application-test.yml`에서 EmbeddedKafka 대신 실제 Kafka 브로커 사용:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092  # 실제 Kafka 브로커 주소
```

### 주의사항

- EmbeddedKafka 어노테이션 제거 필요
- 테스트 전에 Kafka 브로커가 실행 중이어야 함

---

## 방법 4: kafka-console-consumer 사용

```bash
# Kafka 컨테이너 내에서 실행
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic stock-service-topic \
  --from-beginning

# 또는 로컬에 Kafka가 설치된 경우
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic stock-service-topic \
  --from-beginning
```

---

## KafkaUI 사용 시 장점

1. **시각적 확인**: 메시지 내용을 JSON 형식으로 쉽게 확인
2. **메타데이터 확인**: Partition, Offset, Timestamp 등 확인 가능
3. **토픽 관리**: 토픽 생성, 삭제, 설정 확인
4. **Consumer 그룹 모니터링**: Consumer 상태 확인
5. **메시지 검색**: 특정 조건으로 메시지 필터링

