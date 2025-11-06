# Kafka 비동기 통신 통합 가이드

## 📋 개요

Order 서비스에서 구현된 Kafka 비동기 통신 구조와 다른 서비스에서 구현해야 할 내용을 정리한 문서입니다.

## ✅ Order 서비스 구현 완료 내용

### 1. Kafka Producer 구현
- ✅ `StockKafkaProducer` - 재고 차감 요청 발행 (`order-created` topic)
- ✅ `DeliveryKafkaProducer` - 배송 요청 발행 (`delivery-request-topic`)
- ✅ `OrderCompletedKafkaProducer` - 주문 완료 메시지 발행 (`order-completed-topic`)

### 2. Kafka Consumer 구현
- ✅ `PaymentSuccessConsumer` - 결제 완료 메시지 수신 (`payment-success-topic`)
- ✅ `DeliveryStartConsumer` - 배송 시작 메시지 수신 (`delivery-start-topic`)
- ✅ `StockResponseConsumer` - 재고 확인 응답 수신 (`stock-response-topic`)

### 3. 메시지 DTO
- ✅ `StockDecreaseRequestMessage` - 재고 차감 요청
- ✅ `DeliveryRequestMessage` - 배송 요청
- ✅ `OrderCompletedMessage` - 주문 완료
- ✅ `PaymentSuccessMessage` - 결제 완료
- ✅ `DeliveryStartMessage` - 배송 시작
- ✅ `StockResponseMessage` - 재고 확인 응답

### 4. OrderService 메서드
- ✅ `handlePaymentSuccess()` - 결제 성공 처리
- ✅ `handlePaymentFailure()` - 결제 실패 처리
- ✅ `handleDeliveryStart()` - 배송 시작 처리

## 📨 다른 서비스에서 구현해야 할 내용

### 1. Stock 서비스

#### 구현해야 할 내용

**Consumer:**
- `order-created` topic에서 `StockDecreaseRequestMessage` 수신
- 재고 차감 처리
- 성공/실패에 따라 `StockResponseMessage` 발행

**Producer:**
- `stock-response-topic`으로 `StockResponseMessage` 발행

**메시지 형식:**
```java
// 수신: StockDecreaseRequestMessage
public record StockDecreaseRequestMessage(
    UUID orderId,
    List<ProductStockRequest> products
) {
    public record ProductStockRequest(UUID productId, int quantity) {}
}

// 발행: StockResponseMessage
public record StockResponseMessage(
    UUID orderId,
    UUID productId,
    String status,  // "SUCCESS" or "FAILED"
    String message
) {}
```

**Topic:**
- 수신: `order-created`
- 발행: `stock-response-topic`

---

### 2. Payment 서비스

#### 구현해야 할 내용

**Producer:**
- 결제 완료 후 `payment-success-topic`으로 `PaymentSuccessMessage` 발행
- 결제 실패 시에도 동일한 topic으로 실패 상태 발행

**메시지 형식:**
```java
// 발행: PaymentSuccessMessage
public record PaymentSuccessMessage(
    UUID orderId,
    String paymentKey,
    Integer amount,
    String status  // "SUCCESS" or "FAILED"
) {}
```

**Topic:**
- 발행: `payment-success-topic`

**발행 시점:**
- PG 결제 승인 후 (동기 통신 완료 후)
- Order 서비스가 비동기로 수신하여 처리

---

### 3. Delivery 서비스

#### 구현해야 할 내용

**Consumer:**
- `delivery-request-topic`에서 `DeliveryRequestMessage` 수신
- 배송 시작 처리
- 처리 완료 후 `DeliveryStartMessage` 발행

**Producer:**
- `delivery-start-topic`으로 `DeliveryStartMessage` 발행

**메시지 형식:**
```java
// 수신: DeliveryRequestMessage
public record DeliveryRequestMessage(
    UUID orderId,
    UUID customerId,
    String address,
    String addressDetail,
    String zipcode,
    String phone,
    String name,
    String deliveryMemo
) {}

// 발행: DeliveryStartMessage
public record DeliveryStartMessage(
    UUID orderId,
    UUID deliveryId,
    String status  // "STARTED" or "FAILED"
) {}
```

**Topic:**
- 수신: `delivery-request-topic`
- 발행: `delivery-start-topic`

**처리 흐름:**
1. Order 서비스에서 배송 요청 발행
2. Delivery 서비스가 메시지 수신
3. 배송 시작 처리
4. 배송 시작 메시지 발행
5. Order 서비스가 수신하여 주문 완료 처리

---

## 🔄 전체 프로세스 흐름

```
1. Client -> Order API (동기)
   └─> 주문 생성, 상태=PENDING
   
2. Order -> Stock (비동기 - Kafka)
   └─> order-created topic으로 재고 차감 요청 발행
   
3. Stock -> Order (비동기 - Kafka)
   └─> stock-response-topic으로 재고 확인 응답 발행
   
4. Order -> Client (동기)
   └─> 주문 정보 반환
   
5. Client -> Payment (비동기)
   └─> 결제 요청
   
6. Payment -> PG (동기)
   └─> 결제 승인 요청
   
7. PG -> Payment (동기)
   └─> 결제 승인 응답
   
8. Payment -> Order (비동기 - Kafka)
   └─> payment-success-topic으로 결제 완료 메시지 발행
   
9. Order -> Delivery (비동기 - Kafka)
   └─> delivery-request-topic으로 배송 요청 발행
   
10. Delivery -> Order (비동기 - Kafka)
    └─> delivery-start-topic으로 배송 시작 메시지 발행
    
11. Order -> Client (비동기 - Kafka)
    └─> order-completed-topic으로 주문 완료 메시지 발행
```

## 📝 Kafka Topic 목록

| Topic | Producer | Consumer | 설명 |
|-------|----------|----------|------|
| `order-created` | Order | Stock | 재고 차감 요청 |
| `stock-response-topic` | Stock | Order | 재고 확인 응답 |
| `payment-success-topic` | Payment | Order | 결제 완료 메시지 |
| `delivery-request-topic` | Order | Delivery | 배송 요청 |
| `delivery-start-topic` | Delivery | Order | 배송 시작 메시지 |
| `order-completed-topic` | Order | Client | 주문 완료 메시지 |

## 🛠️ 구현 체크리스트

### Stock 서비스
- [ ] `order-created` topic Consumer 구현
- [ ] `StockDecreaseRequestMessage` 역직렬화 설정
- [ ] 재고 차감 로직 구현
- [ ] `stock-response-topic` Producer 구현
- [ ] `StockResponseMessage` 직렬화 설정

### Payment 서비스
- [ ] `payment-success-topic` Producer 구현
- [ ] `PaymentSuccessMessage` 직렬화 설정
- [ ] 결제 완료/실패 시 메시지 발행 로직 추가

### Delivery 서비스
- [ ] `delivery-request-topic` Consumer 구현
- [ ] `DeliveryRequestMessage` 역직렬화 설정
- [ ] 배송 시작 처리 로직 구현
- [ ] `delivery-start-topic` Producer 구현
- [ ] `DeliveryStartMessage` 직렬화 설정

## 📚 참고 자료

- Kafka 설정: `src/main/resources/application.yml`
- Docker Compose: `src/main/resources/docker-compose.yml`
- 메시지 DTO: `src/main/java/profect/group1/goormdotcom/order/infrastructure/kafka/dto/`
- Producer 예제: `src/main/java/profect/group1/goormdotcom/order/infrastructure/kafka/service/`
- Consumer 예제: `src/main/java/profect/group1/goormdotcom/order/infrastructure/kafka/consumer/`

## ⚠️ 주의사항

1. **메시지 형식 통일**: Order 서비스의 DTO와 동일한 구조로 구현해야 합니다.
2. **Topic 이름**: 정확한 topic 이름을 사용해야 합니다.
3. **에러 처리**: 메시지 처리 실패 시 재시도 또는 Dead Letter Queue 고려
4. **트랜잭션**: Kafka 메시지 발행과 DB 작업의 트랜잭션 일관성 고려

## 📞 문의

구현 시 문제가 발생하면 Order 서비스 담당자와 협의하세요.

