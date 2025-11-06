package profect.group1.goormdotcom.order.infrastructure.kafka.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.OrderCompletedMessage;

/**
 * 주문 완료 메시지를 Kafka로 발행하는 Producer 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompletedKafkaProducer {

    private static final String ORDER_COMPLETED_TOPIC = "order-completed-topic";
    
    private final KafkaTemplate<String, OrderCompletedMessage> kafkaTemplate;

    /**
     * 주문 완료 메시지를 Kafka로 비동기 발행
     */
    public void sendOrderCompleted(OrderCompletedMessage message) {
        String key = message.orderId().toString();
        
        log.info("Kafka 주문 완료 메시지 발행: topic={}, orderId={}, customerId={}", 
            ORDER_COMPLETED_TOPIC, message.orderId(), message.customerId());
        
        CompletableFuture<SendResult<String, OrderCompletedMessage>> future = 
            kafkaTemplate.send(ORDER_COMPLETED_TOPIC, key, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka 주문 완료 메시지 발행 성공: orderId={}, offset={}", 
                    message.orderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Kafka 주문 완료 메시지 발행 실패: orderId={}, error={}", 
                    message.orderId(), ex.getMessage(), ex);
            }
        });
    }
}

