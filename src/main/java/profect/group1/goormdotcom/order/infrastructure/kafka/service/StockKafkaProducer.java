package profect.group1.goormdotcom.order.infrastructure.kafka.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockDecreaseRequestMessage;

/**
 * 재고 차감 요청을 Kafka로 발행하는 Producer 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockKafkaProducer {

    private static final String STOCK_DECREASE_TOPIC = "order-created";
    
    private final KafkaTemplate<String, StockDecreaseRequestMessage> kafkaTemplate;

    /**
     * 재고 차감 요청을 Kafka로 비동기 발행
     * 
     * @param orderId 주문 ID
     * @param products 재고 차감할 상품 목록 (productId, quantity)
     */
    public void sendStockDecreaseRequest(UUID orderId, 
                                         List<StockDecreaseRequestMessage.ProductStockRequest> products) {
        StockDecreaseRequestMessage message = new StockDecreaseRequestMessage(orderId, products);
        
        String key = orderId.toString();
        
        log.info("Kafka 재고 차감 요청 발행: topic={}, orderId={}, productCount={}", 
            STOCK_DECREASE_TOPIC, orderId, products.size());
        
        CompletableFuture<SendResult<String, StockDecreaseRequestMessage>> future = 
            kafkaTemplate.send(STOCK_DECREASE_TOPIC, key, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka 재고 차감 요청 발행 성공: orderId={}, offset={}", 
                    orderId, result.getRecordMetadata().offset());
            } else {
                log.error("Kafka 재고 차감 요청 발행 실패: orderId={}, error={}", 
                    orderId, ex.getMessage(), ex);
                // TODO: 실패 시 재시도 로직 또는 Dead Letter Queue 처리 고려
            }
        });
    }
}

