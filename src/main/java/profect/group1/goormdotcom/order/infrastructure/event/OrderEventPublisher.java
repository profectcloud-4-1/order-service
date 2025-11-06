package profect.group1.goormdotcom.order.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.DeliveryStartedEvent;
import profect.group1.goormdotcom.order.domain.event.OrderCreatedEvent;
import profect.group1.goormdotcom.order.domain.event.PaymentCompletedEvent;
import profect.group1.goormdotcom.order.domain.event.StockDecreaseRequestedEvent;

/**
 * 주문 도메인 이벤트 발행자
 * 이벤트 기반 아키텍처를 위한 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문 생성 이벤트 발행
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 발행: orderId={}, customerId={}", 
            event.getOrderId(), event.getCustomerId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 재고 차감 요청 이벤트 발행
     */
    public void publishStockDecreaseRequested(StockDecreaseRequestedEvent event) {
        log.info("재고 차감 요청 이벤트 발행: orderId={}, productCount={}", 
            event.getOrderId(), event.getProducts().size());
        eventPublisher.publishEvent(event);
    }

    /**
     * 결제 완료 이벤트 발행 (외부에서 수신한 이벤트를 도메인 이벤트로 변환)
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 발행: orderId={}, status={}", 
            event.getOrderId(), event.getStatus());
        eventPublisher.publishEvent(event);
    }

    /**
     * 배송 시작 이벤트 발행 (외부에서 수신한 이벤트를 도메인 이벤트로 변환)
     */
    public void publishDeliveryStarted(DeliveryStartedEvent event) {
        log.info("배송 시작 이벤트 발행: orderId={}, deliveryId={}", 
            event.getOrderId(), event.getDeliveryId());
        eventPublisher.publishEvent(event);
    }
}


