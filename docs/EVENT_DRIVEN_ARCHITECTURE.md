# 이벤트 기반 아키텍처 (Event-Driven Architecture)

## 📋 개요

Order 서비스의 비동기 통신을 이벤트 기반 아키텍처로 구현했습니다. Kafka를 메시지 브로커로 사용하되, 도메인 이벤트를 통해 느슨한 결합을 달성합니다.

## 🏗️ 아키텍처 구조

### 이벤트 흐름

```
[도메인 로직] 
    ↓
[이벤트 발행] (OrderEventPublisher)
    ↓
[이벤트 핸들러] (@EventListener)
    ↓
[Kafka Producer] (비동기 발행)
    ↓
[Kafka Topic]
    ↓
[Kafka Consumer] (외부 서비스에서 수신)
    ↓
[도메인 이벤트 변환]
    ↓
[이벤트 핸들러] (@EventListener)
    ↓
[비즈니스 로직 처리]
```

## 📦 구현된 컴포넌트

### 1. 도메인 이벤트 (Domain Events)

**위치**: `src/main/java/profect/group1/goormdotcom/order/domain/event/`

- `OrderCreatedEvent` - 주문 생성 이벤트
- `PaymentCompletedEvent` - 결제 완료 이벤트
- `DeliveryStartedEvent` - 배송 시작 이벤트
- `StockDecreaseRequestedEvent` - 재고 차감 요청 이벤트

### 2. 이벤트 발행자 (Event Publisher)

**위치**: `src/main/java/profect/group1/goormdotcom/order/infrastructure/event/OrderEventPublisher.java`

- Spring의 `ApplicationEventPublisher`를 래핑
- 도메인 이벤트를 발행하는 통합 인터페이스 제공

### 3. 이벤트 핸들러 (Event Handlers)

**위치**: `src/main/java/profect/group1/goormdotcom/order/infrastructure/event/handler/`

- `OrderCreatedEventHandler` - 주문 생성 시 재고 차감 요청 발행
- `PaymentCompletedEventHandler` - 결제 완료 시 배송 요청 처리
- `DeliveryStartedEventHandler` - 배송 시작 시 주문 완료 처리

**특징**:
- `@EventListener` 어노테이션 사용
- `@Async`로 비동기 처리
- 이벤트를 받아서 Kafka로 발행하거나 비즈니스 로직 처리

### 4. Kafka Consumer (이벤트 변환)

**위치**: `src/main/java/profect/group1/goormdotcom/order/infrastructure/kafka/consumer/`

- `PaymentSuccessConsumer` - Kafka 메시지 → `PaymentCompletedEvent`
- `DeliveryStartConsumer` - Kafka 메시지 → `DeliveryStartedEvent`
- `StockResponseConsumer` - 재고 응답 처리

**역할**: 외부 서비스에서 받은 Kafka 메시지를 도메인 이벤트로 변환

## 🔄 이벤트 흐름 예시

### 주문 생성 프로세스

```
1. OrderService.create()
   └─> OrderCreatedEvent 발행
   
2. OrderCreatedEventHandler.handleOrderCreated()
   └─> StockKafkaProducer.sendStockDecreaseRequest()
   └─> Kafka topic: "order-created"
   
3. Stock 서비스가 메시지 수신
   └─> 재고 차감 처리
   └─> stock-response-topic으로 응답 발행
```

### 결제 완료 프로세스

```
1. Payment 서비스
   └─> payment-success-topic으로 메시지 발행
   
2. PaymentSuccessConsumer.consumePaymentSuccess()
   └─> PaymentCompletedEvent로 변환
   └─> eventPublisher.publishPaymentCompleted()
   
3. PaymentCompletedEventHandler.handlePaymentCompleted()
   └─> OrderService.handlePaymentSuccess()
   └─> DeliveryKafkaProducer.sendDeliveryRequest()
   └─> Kafka topic: "delivery-request-topic"
```

### 배송 시작 프로세스

```
1. Delivery 서비스
   └─> delivery-start-topic으로 메시지 발행
   
2. DeliveryStartConsumer.consumeDeliveryStart()
   └─> DeliveryStartedEvent로 변환
   └─> eventPublisher.publishDeliveryStarted()
   
3. DeliveryStartedEventHandler.handleDeliveryStarted()
   └─> OrderService.handleDeliveryStart()
   └─> OrderCompletedKafkaProducer.sendOrderCompleted()
   └─> Kafka topic: "order-completed-topic"
```

## 🎯 이벤트 기반 아키텍처의 장점

### 1. 느슨한 결합 (Loose Coupling)
- 서비스 간 직접 의존성 제거
- 이벤트를 통한 간접 통신

### 2. 확장성 (Scalability)
- 새로운 이벤트 핸들러 추가 용이
- 여러 핸들러가 동일 이벤트 처리 가능

### 3. 유지보수성 (Maintainability)
- 도메인 이벤트로 비즈니스 의도 명확화
- 이벤트 흐름 추적 용이

### 4. 테스트 용이성
- 이벤트 모킹으로 단위 테스트 용이
- 통합 테스트에서 이벤트 검증 가능

## 📝 사용 예시

### 이벤트 발행

```java
// OrderService에서
OrderCreatedEvent event = new OrderCreatedEvent(
    orderId,
    customerId,
    productItems
);
eventPublisher.publishOrderCreated(event);
```

### 이벤트 핸들링

```java
@Async
@EventListener
public void handleOrderCreated(OrderCreatedEvent event) {
    // 비즈니스 로직 처리
    // Kafka로 발행 등
}
```

## 🔧 설정

### application.yml

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    producer:
      bootstrap-servers: localhost:9092
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### 비동기 처리 활성화

`@EnableAsync`가 `GoormdotcomApplication`에 설정되어 있어 이벤트 핸들러는 비동기로 실행됩니다.

## 🚀 확장 방법

### 새로운 이벤트 추가

1. 도메인 이벤트 클래스 생성
2. `OrderEventPublisher`에 발행 메서드 추가
3. 이벤트 핸들러 생성 (`@EventListener`)
4. 필요시 Kafka Producer/Consumer 추가

### 예시: 주문 취소 이벤트

```java
// 1. 이벤트 정의
public class OrderCancelledEvent {
    private final UUID orderId;
    private final String reason;
}

// 2. 이벤트 발행
eventPublisher.publishOrderCancelled(new OrderCancelledEvent(orderId, reason));

// 3. 이벤트 핸들러
@Async
@EventListener
public void handleOrderCancelled(OrderCancelledEvent event) {
    // 주문 취소 처리
}
```

## 📚 참고 자료

- Spring Events: https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events
- Kafka Integration: `docs/KAFKA_INTEGRATION_GUIDE.md`
- MSA 전환 전략: `docs/MSA_MIGRATION.md`


