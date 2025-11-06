package profect.group1.goormdotcom.order.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.DeliveryStartedEvent;
import profect.group1.goormdotcom.order.infrastructure.event.OrderEventPublisher;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.DeliveryStartMessage;

/**
 * 배송 시작 메시지를 수신하는 Consumer (Delivery -> Order)
 * Kafka 메시지를 도메인 이벤트로 변환하여 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryStartConsumer {

    private static final String TOPIC = "delivery-start-topic";
    private static final String GROUP_ID = "order-group";

    private final OrderEventPublisher eventPublisher;

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID, containerFactory = "deliveryKafkaListenerContainerFactory")
    public void consumeDeliveryStart(DeliveryStartMessage message) {
        log.info("배송 시작 메시지 수신: orderId={}, deliveryId={}, status={}", 
            message.orderId(), message.deliveryId(), message.status());

        try {
            // Kafka 메시지를 도메인 이벤트로 변환하여 발행
            DeliveryStartedEvent event = new DeliveryStartedEvent(
                message.orderId(),
                message.deliveryId(),
                message.status()
            );
            
            eventPublisher.publishDeliveryStarted(event);
            log.info("배송 시작 이벤트 발행 완료: orderId={}", message.orderId());
        } catch (Exception e) {
            log.error("배송 시작 메시지 처리 실패: orderId={}, error={}", 
                message.orderId(), e.getMessage(), e);
        }
    }
}

