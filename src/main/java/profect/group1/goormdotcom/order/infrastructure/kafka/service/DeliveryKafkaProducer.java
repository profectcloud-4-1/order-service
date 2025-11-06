package profect.group1.goormdotcom.order.infrastructure.kafka.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.DeliveryRequestMessage;

/**
 * 배송 요청을 Kafka로 발행하는 Producer 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryKafkaProducer {

    private static final String DELIVERY_REQUEST_TOPIC = "delivery-request-topic";
    
    private final KafkaTemplate<String, DeliveryRequestMessage> kafkaTemplate;

    /**
     * 배송 요청을 Kafka로 비동기 발행
     */
    public void sendDeliveryRequest(DeliveryRequestMessage message) {
        String key = message.orderId().toString();
        
        log.info("Kafka 배송 요청 발행: topic={}, orderId={}", 
            DELIVERY_REQUEST_TOPIC, message.orderId());
        
        CompletableFuture<SendResult<String, DeliveryRequestMessage>> future = 
            kafkaTemplate.send(DELIVERY_REQUEST_TOPIC, key, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka 배송 요청 발행 성공: orderId={}, offset={}", 
                    message.orderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Kafka 배송 요청 발행 실패: orderId={}, error={}", 
                    message.orderId(), ex.getMessage(), ex);
            }
        });
    }
}

