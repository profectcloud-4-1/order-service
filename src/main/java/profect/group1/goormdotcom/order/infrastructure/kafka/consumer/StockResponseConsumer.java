package profect.group1.goormdotcom.order.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockResponseMessage;
import profect.group1.goormdotcom.order.service.OrderService;

/**
 * 재고 확인 응답을 수신하는 Consumer (Stock -> Order)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockResponseConsumer {

    private static final String TOPIC = "stock-response-topic";
    private static final String GROUP_ID = "order-group";

    private final OrderService orderService;

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID, containerFactory = "stockResponseKafkaListenerContainerFactory")
    public void consumeStockResponse(StockResponseMessage message) {
        log.info("재고 확인 응답 수신: orderId={}, productId={}, status={}", 
            message.orderId(), message.productId(), message.status());

        try {
            if ("SUCCESS".equals(message.status())) {
                log.info("재고 차감 성공: orderId={}, productId={}", 
                    message.orderId(), message.productId());
            } else {
                log.warn("재고 차감 실패: orderId={}, productId={}, message={}", 
                    message.orderId(), message.productId(), message.message());
                // TODO: 재고 차감 실패 시 주문 취소 처리 고려
            }
        } catch (Exception e) {
            log.error("재고 확인 응답 처리 실패: orderId={}, error={}", 
                message.orderId(), e.getMessage(), e);
        }
    }
}

