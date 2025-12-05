package profect.group1.goormdotcom.delivery.event;

import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.service.DeliveryService;
import profect.group1.goormdotcom.order.event.Delivery.DeliveryCancellationRequestedEvent;
import profect.group1.goormdotcom.order.event.Delivery.DeliveryRequestedEvent;

/**
 * 주문 서비스에서 발행하는 배송 관련 이벤트를 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener implements DeliveryEventHandlerInterface {

    private final DeliveryService deliveryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Async
    @EventListener
    public void onDeliveryRequested(DeliveryRequestedEvent event) {
        log.info("배송 요청 이벤트 수신: orderId={}, occurredAt={}", event.orderId(), event.occurredAt());
        System.out.println("🎯 이벤트 수신: " + event.orderId());
        System.out.println("🎯 발생 시간: " + event.occurredAt());
        Delivery delivery = deliveryService.startDelivery(
            event.orderId(),
            event.customerId(),
            event.address(),
            event.addressDetail(),
            event.zipcode(),
            event.phone(),
            event.name(),
            event.deliveryMemo()
        );
        log.info("배송 생성 완료: orderId={}", event.orderId());

        UUID deliveryId = delivery != null ? delivery.getId() : null;
        applicationEventPublisher.publishEvent(
            DeliveryStartedEvent.builder()
                .orderId(event.orderId())
                .deliveryId(deliveryId)
                .occurredAt(Instant.now())
                .build()
        );
        log.info("배송 시작 이벤트 발행: orderId={}", event.orderId());
    }

    @Override
    @Async
    @EventListener
    public void onDeliveryCancellationRequested(DeliveryCancellationRequestedEvent event) {
        log.info("배송 취소 이벤트 수신: orderId={}, occurredAt={}", event.orderId(), event.occurredAt());
        System.out.println("🎯 이벤트 수신: " + event.orderId());
        System.out.println("🎯 발생 시간: " + event.occurredAt());
        try {
            deliveryService.cancel(event.orderId());
            log.info("배송 취소 완료: orderId={}", event.orderId());
        } catch (IllegalArgumentException ex) {
            log.warn("배송 취소 이벤트 처리 중 예외 발생: orderId={}, message={}", event.orderId(), ex.getMessage());
        }
    }
}

