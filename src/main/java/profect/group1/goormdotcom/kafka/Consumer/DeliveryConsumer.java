package profect.group1.goormdotcom.kafka.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.event.Delivery.DeliveryRequestedEvent;
import profect.group1.goormdotcom.delivery.service.DeliveryService;

/**
 * 배송 서비스에서 발행한 이벤트를 수신하는 Kafka Consumer
 * TODO: 실제 구현 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryConsumer {
    private final ObjectMapper objectMapper;
    private final DeliveryService deliveryService;

    @KafkaListener(topics = "delivery-service-topic", groupId = "order-service-delivery-group")
    public void DeliveryRequestedEvent(String message) {
        try {
            DeliveryRequestedEvent event = objectMapper.readValue(message, DeliveryRequestedEvent.class);
            log.info("배송 요청 이벤트 수신: orderId={}", event.orderId());
            deliveryService.startDelivery(event.orderId(), event.customerId(), event.address(), event.addressDetail(), event.zipcode(), event.phone(), event.name(), event.deliveryMemo());
            log.info("배송 요청에 따라 주문 상태를 DELIVERY_REQUESTED로 갱신: orderId={}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("배송 요청 이벤트 역직렬화 실패: message={}", message, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
}