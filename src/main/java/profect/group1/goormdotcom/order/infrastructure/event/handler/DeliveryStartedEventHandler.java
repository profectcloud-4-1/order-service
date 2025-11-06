package profect.group1.goormdotcom.order.infrastructure.event.handler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.DeliveryStartedEvent;
import profect.group1.goormdotcom.order.service.OrderService;

/**
 * 배송 시작 이벤트 핸들러
 * 이벤트를 받아서 주문 서비스 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryStartedEventHandler {

    private final OrderService orderService;

    @Async
    @EventListener
    public void handleDeliveryStarted(DeliveryStartedEvent event) {
        log.info("배송 시작 이벤트 처리 시작: orderId={}, deliveryId={}", 
            event.getOrderId(), event.getDeliveryId());

        try {
            if ("STARTED".equals(event.getStatus())) {
                orderService.handleDeliveryStart(event.getOrderId());
                log.info("배송 시작 이벤트 처리 완료: orderId={}", event.getOrderId());
            } else {
                log.warn("배송 시작 실패: orderId={}, status={}", 
                    event.getOrderId(), event.getStatus());
            }
        } catch (Exception e) {
            log.error("배송 시작 이벤트 처리 실패: orderId={}, error={}", 
                event.getOrderId(), e.getMessage(), e);
        }
    }
}


