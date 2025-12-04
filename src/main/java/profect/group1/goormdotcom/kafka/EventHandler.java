package profect.group1.goormdotcom.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import profect.group1.goormdotcom.kafka.StockProducer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandler {
    private final DeliveryProducer deliveryProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleDeliveryStartedEvent(DeliveryStartedEvent event) {
        log.info("배송 시작 이벤트 수신: orderId={}", event.orderId());
        deliveryProducer.sendDeliveryStartedEvent("order-service-topic", event);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async
    public void handleDeliveryStartFailedEvent(DeliveryStartFailedEvent event) {
        log.info("배송 시작 실패 이벤트 수신: orderId={}", event.orderId());
        deliveryProducer.sendDeliveryStartFailedEvent("order-service-topic", event);
    }
}