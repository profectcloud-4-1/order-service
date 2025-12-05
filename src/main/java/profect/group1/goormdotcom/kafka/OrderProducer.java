package profect.group1.goormdotcom.kafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.event.Delivery.DeliveryRequestedEvent;
import profect.group1.goormdotcom.order.event.Stock.StockRollbackRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String topic, DeliveryRequestedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message);
            log.info("Kafka 메시지 발행 완료: topic={}, orderId={}", topic, event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 직렬화 실패: topic={}, orderId={}", topic, event.orderId(), e);
            throw new RuntimeException("Kafka 메시지 발행 실패", e);
        }
    }
    public void send(String topic, StockRollbackRequestedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message);
            log.info("Kafka 메시지 발행 완료: topic={}, orderId={}", topic, event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 직렬화 실패: topic={}, orderId={}", topic, event.orderId(), e);
            throw new RuntimeException("Kafka 메시지 발행 실패", e);
        }
    }
}   