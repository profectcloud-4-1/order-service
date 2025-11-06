package profect.group1.goormdotcom.order.infrastructure.event.handler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.PaymentCompletedEvent;
import profect.group1.goormdotcom.order.service.OrderService;

/**
 * 결제 완료 이벤트 핸들러
 * 이벤트를 받아서 주문 서비스 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedEventHandler {

    private final OrderService orderService;

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리 시작: orderId={}, status={}", 
            event.getOrderId(), event.getStatus());

        try {
            if ("SUCCESS".equals(event.getStatus())) {
                orderService.handlePaymentSuccess(event.getOrderId());
            } else {
                orderService.handlePaymentFailure(event.getOrderId());
            }
            log.info("결제 완료 이벤트 처리 완료: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}, error={}", 
                event.getOrderId(), e.getMessage(), e);
        }
    }
}


