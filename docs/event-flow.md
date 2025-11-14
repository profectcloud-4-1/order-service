# 주문 ↔︎ 배송 이벤트 흐름

## 전체 개요
- 결제 성공·실패 시 주문 서비스(`OrderService`)는 배송 서비스와 동기 호출 대신 **도메인 이벤트**를 주고받습니다.
- 이벤트 발행은 `DeliveryEventPublisherInterface` 추상화 뒤에 숨겨져 있고, 실제 구현 `ApplicationDeliveryEventPublisher`는 `@Async` 기반으로 Spring `ApplicationEventPublisher`에 이벤트를 전달합니다.
- 배송 서비스는 `DeliveryEventListener`가 `DeliveryEventHandlerInterface`를 구현하며, `@EventListener` + `@Async` 조합으로 별도 스레드에서 비즈니스 로직을 처리합니다.

## 시나리오

### 시나리오 1: 정상 결제 완료 → 배송 생성 성공
**상황**: 고객이 주문을 완료하고 결제에 성공한 경우

**흐름**:
1. 고객이 주문을 생성하고 결제를 완료
2. `OrderService.completePayment(orderId)` 호출
3. 주문 상태가 `PENDING` → `PAID`로 변경
4. `DeliveryRequestedEvent` 비동기 발행 (주문 응답은 즉시 반환)
5. 배송 서비스가 이벤트를 수신하여 배송 생성 (`DeliveryService.startDelivery()`)
6. 배송 생성 완료 후 `DeliveryStartedEvent` 발행
7. 주문 서비스가 이벤트를 수신하여 주문 상태를 `PAID` → `COMPLETED`로 변경

**결과**: 
- 고객은 즉시 주문 완료 응답을 받음
- 배송 생성은 백그라운드에서 비동기로 처리됨
- 최종적으로 주문 상태가 `COMPLETED`로 변경됨

**시간 흐름**:
```
[고객] 주문 요청
    ↓ (동기)
[주문 서비스] 주문 생성 (PENDING)
    ↓ (동기)
[주문 서비스] 결제 완료 처리 (PAID) + DeliveryRequestedEvent 발행
    ↓ (비동기, 즉시 응답 반환)
[고객] 주문 완료 응답 수신 ✅
    ↓ (비동기, 백그라운드)
[배송 서비스] 배송 생성 + DeliveryStartedEvent 발행
    ↓ (비동기, 백그라운드)
[주문 서비스] 주문 상태 COMPLETED로 변경
```

### 시나리오 2: 결제 실패 → 배송 취소
**상황**: 결제가 실패하여 주문이 취소되는 경우

**흐름**:
1. 결제 실패로 `OrderService.failPayment(orderId)` 호출
2. 재고 원복 처리 (Stock 와 연동(외부 서비스 -> 기존 로직 유지))
3. 주문 상태가 `PENDING` → `FAILED`로 변경
4. `DeliveryCancellationRequestedEvent` 비동기 발행
5. 배송 서비스가 이벤트를 수신하여 배송 취소 처리 (`DeliveryService.cancel()`)

**결과**:
- 재고가 원복됨
- 주문 상태가 `FAILED`로 변경됨
- 배송이 생성되지 않았거나 이미 생성된 경우 취소 처리됨

**시간 흐름**:
```
[결제 서비스] 결제 실패 통지
    ↓ (동기)
[주문 서비스] 재고 원복 + 주문 상태 FAILED + DeliveryCancellationRequestedEvent 발행
    ↓ (비동기)
[배송 서비스] 배송 취소 처리
```

### 시나리오 3: 트랜잭션 롤백 → 보상 이벤트 발행
**상황**: 결제 완료 처리 중 예외 발생으로 트랜잭션이 롤백되는 경우

**흐름**:
1. `OrderService.completePayment(orderId)` 실행 시작
2. `TransactionSynchronization` 등록 (롤백 감지용)
3. 주문 상태를 `PAID`로 변경 시도
4. `DeliveryRequestedEvent` 비동기 발행
5. 이후 예외 발생으로 트랜잭션 롤백
6. `TransactionSynchronization.afterCompletion(STATUS_ROLLED_BACK)` 호출
7. `DeliveryCancellationRequestedEvent` 보상 이벤트 발행
8. 배송 서비스가 보상 이벤트를 수신하여 배송 취소 처리

**결과**:
- 주문 상태 변경이 롤백됨 (여전히 `PENDING`)
- 이미 발행된 `DeliveryRequestedEvent`에 대한 보상으로 배송 취소 이벤트 발행
- 데이터 일관성 유지

**시간 흐름**:
```
[주문 서비스] completePayment 시작
    ↓
[주문 서비스] TransactionSynchronization 등록
    ↓
[주문 서비스] 주문 상태 PAID 변경 + DeliveryRequestedEvent 발행
    ↓ (비동기)
[배송 서비스] 배송 생성 시작...
    ↓ (예외 발생)
[주문 서비스] 트랜잭션 롤백
    ↓
[주문 서비스] afterCompletion(STATUS_ROLLED_BACK) → DeliveryCancellationRequestedEvent 발행
    ↓ (비동기)
[배송 서비스] 배송 취소 처리 (보상 트랜잭션)
```

**중요 포인트**:
- `DeliveryRequestedEvent`는 비동기로 발행되므로 트랜잭션 롤백 전에 이미 배송 서비스에 전달될 수 있음
- 보상 이벤트를 통해 이미 처리된 배송 생성 작업을 취소하여 일관성 유지
- Saga 패턴의 보상 트랜잭션(Compensating Transaction) 패턴 일부 차용

## 결제 성공 → 배송 생성
1. `OrderService.completePayment(orderId)`  
   - 주문 상태를 `PAID`로 기록(배송 시작 이벤트 수신 시 `COMPLETED`로 전환)  
   - 배송지 정보를 이용해 `DeliveryRequestedEvent` 생성  
   - `deliveryEventPublisher.publishDeliveryRequested(event)` 호출
2. `ApplicationDeliveryEventPublisher.publishDeliveryRequested`  
   - `@Async`로 별도 스레드에서 실행  
   - `ApplicationEventPublisher.publishEvent(event)` 실행
3. `DeliveryEventListener.onDeliveryRequested` (`@EventListener` + `@Async`)  
   - 별도 스레드에서 이벤트 수신  
   - `deliveryService.startDelivery(...)` 호출로 배송 생성  
   - 배송 생성이 완료되면 `DeliveryStartedEvent`를 발행
4. `OrderDeliveryStatusEventListener.onDeliveryStarted` (`@Async` + `@EventListener` + `@Transactional`)  
   - 이벤트를 별도 스레드에서 수신한 뒤 **독립적인 새 트랜잭션**으로 주문 상태를 `COMPLETED`로 갱신하고 `OrderStatus` 이력을 저장
   - `@Async`로 인해 별도 스레드에서 실행되므로 원래 트랜잭션(`completePayment`)과 완전히 분리된 독립적인 트랜잭션에서 동작
   - 이벤트 리스너의 트랜잭션 실패는 원래 트랜잭션에 영향을 주지 않음

## 결제 실패 → 배송 취소
1. `OrderService.failPayment(orderId)`  
   - 재고 원복 성공 시 주문 상태를 `FAILED`로 기록  
   - `DeliveryCancellationRequestedEvent` 발행
2. `ApplicationDeliveryEventPublisher.publishDeliveryCancellationRequested`  
   - `@Async`로 별도 스레드에서 실행  
   - `ApplicationEventPublisher.publishEvent(event)` 실행
3. `DeliveryEventListener.onDeliveryCancellationRequested` (`@EventListener` + `@Async`)  
   - 별도 스레드에서 이벤트 수신  
   - `deliveryService.cancel(orderId)` 호출 (실패 시 경고 로그만 남김)

## 트랜잭션 롤백 → 보상 이벤트
1. `OrderService.completePayment(orderId)` 실행 중  
   - `TransactionSynchronizationManager.registerSynchronization()`으로 롤백 감지 콜백 등록  
   - 주문 상태를 `PAID`로 기록  
   - `DeliveryRequestedEvent` 비동기 발행
2. 트랜잭션 롤백 발생 시  
   - `TransactionSynchronization.afterCompletion(STATUS_ROLLED_BACK)` 호출  
   - `DeliveryCancellationRequestedEvent` 보상 이벤트 발행 (이미 발행된 배송 요청 이벤트에 대한 보상)
3. `ApplicationDeliveryEventPublisher.publishDeliveryCancellationRequested`  
   - `@Async`로 별도 스레드에서 실행  
   - `ApplicationEventPublisher.publishEvent(event)` 실행
4. `DeliveryEventListener.onDeliveryCancellationRequested`  
   - 별도 스레드에서 이벤트 수신  
   - `deliveryService.cancel(orderId)` 호출로 배송 취소 처리

## 기타 동기 호출
- 주문 취소(배송 전)나 반송 처리 등은 여전히 `DeliveryClient`/`PaymentClient`를 통해 동기로 호출합니다.  
- 배송에서 반송 완료를 주문에 알릴 때는 `DeliveryOrderClient`가 Feign 기반으로 `OrderService.deliveryReturnCompleted`를 호출합니다.

## 테스트
- `OrderDeliveryIntegrationTest`  
  - `completePayment_publishDeliveryRequestedEvent`: 배송 생성 이벤트 전달과 `DeliveryStartedEvent` 수신 후 주문 상태가 `COMPLETED`로 바뀌는지 검증  
  - `failPayment_publishDeliveryCancellationEvent`: 배송 취소 이벤트 전달을 검증  
  - `rollbackDuringCompletePayment_publishesCompensationEvent`: 트랜잭션 롤백 시 보상 이벤트(`DeliveryCancellationRequestedEvent`)가 발행되어 배송 취소가 호출되는지 검증
- `OrderServiceTest`  
  - 이벤트 추상화(`DeliveryEventPublisherInterface`)를 모킹하여 결제 성공/실패 시 올바른 이벤트가 발행되는지 확인  
  - `completePayment_success` 테스트에서 주문 상태가 `PAID`로 저장되는지 추가 검증하고, `failPayment_success` 테스트에 `publishDeliveryCancellationRequested` 검증을 추가하여 이벤트 발행이 실제로 호출되는지 명시적으로 확인  
  - 재고 차감/복구에 대한 기존 모킹은 유지하지만, 이벤트 발행 검증 로직이 추가되면서 테스트가 이벤트 흐름을 중심으로 구성됨

## 테스트용 환경 설정(`application-test.yml`)
- `spring.config.activate.on-profile: test` 로 테스트 실행 시 전용 설정을 적용  
- H2 인메모리 데이터베이스(`jdbc:h2:mem:order-service-test`, MySQL 모드)와 JPA `ddl-auto: create-drop`으로 테스트마다 스키마를 새로 생성  
- OpenFeign 기본 타임아웃을 1초로 제한해 외부 호출 모킹 시 빠르게 실패하도록 구성  
- `service.*.url`을 로컬 목 엔드포인트로 지정해 통합 테스트에서 실제 외부 서비스 호출이 발생하지 않도록 차단  
- 로깅 레벨은 `root: WARN`으로 낮춰 테스트 출력 노이즈를 줄임

