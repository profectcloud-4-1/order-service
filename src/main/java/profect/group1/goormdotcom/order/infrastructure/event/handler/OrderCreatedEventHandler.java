package profect.group1.goormdotcom.order.infrastructure.event.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.OrderCreatedEvent;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockDecreaseRequestMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.service.StockKafkaProducer;

/**
 * 주문 생성 이벤트 핸들러
 * 이벤트를 받아서 Kafka로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler {

    private final StockKafkaProducer stockKafkaProducer;

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 처리 시작: orderId={}", event.getOrderId());

        // 재고 차감 요청 메시지 생성
        List<StockDecreaseRequestMessage.ProductStockRequest> stockRequests = 
            event.getProducts().stream()
                .map(product -> new StockDecreaseRequestMessage.ProductStockRequest(
                    product.getProductId(),
                    product.getQuantity()
                ))
                .collect(Collectors.toList());

        // Kafka로 발행
        stockKafkaProducer.sendStockDecreaseRequest(event.getOrderId(), stockRequests);
        log.info("주문 생성 이벤트 처리 완료: orderId={}", event.getOrderId());
    }
}


