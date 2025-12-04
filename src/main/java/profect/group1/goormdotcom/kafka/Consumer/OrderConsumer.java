package profect.group1.goormdotcom.kafka.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.service.OrderService;
import profect.group1.goormdotcom.order.event.Stock.StockRollbackCompletedEvent;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    /**
     * 재고 서비스에서 재고 롤백 완료 이벤트를 수신하여 주문 상태를 갱신한다.
     * 재고 롤백이 완료되었으므로 주문 상태를 FAILED로 변경한다.
     */
    @KafkaListener(topics = "order-service-topic", groupId = "order-service-group")
    @Transactional
    public void handleStockRollbackCompletedEvent(String message) {
        try {
            StockRollbackCompletedEvent event = objectMapper.readValue(message, StockRollbackCompletedEvent.class);
            log.info("재고 롤백 완료 이벤트 수신: orderId={}", event.orderId());
            
            // 주문 상태를 FAILED로 변경 (재고 롤백 완료 = 결제 실패 처리 완료)
            orderService.appendOrderStatus(event.orderId(), OrderStatus.FAILED);
            
            log.info("재고 롤백 완료에 따라 주문 상태를 FAILED로 갱신: orderId={}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("재고 롤백 완료 이벤트 역직렬화 실패: message={}", message, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
    @kafkaListener(topics = "order-service-topic", groupId = "order-service-delivery-group")
    @Transactional
    public void handleStockRollbackFailedEvent(String message){
        try{
            StockRollbackFailedEvent event = objectMapper.readValue(message, StockRollbackFailedEvent.class);
            log.info("재고 롤백 실패 이벤트 수신: orderId={}", event.orderId());
            orderService.appendOrderStatus(event.orderId(), OrderStatus.FAILED);
            log.info("재고 롤백 실패에 따라 주문 상태를 FAILED로 갱신: orderId={}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("재고 롤백 실패 이벤트 역직렬화 실패: message={}", message, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
    @KafkaListener(topics = "order-service-topic", groupId = "order-service-delivery-group")
    public void handleDeliveryStartedEvent(String message){
        try{
            DeliveryStartedEvent event = objectMapper.readValue(message, DeliveryStartedEvent.class);
            log.info("배송 시작 이벤트 수신: orderId={}", event.orderId());
            orderService.appendOrderStatus(event.orderId(), OrderStatus.COMPLETED);
            log.info("배송 완료에 따라 주문 상태를 COMPLETED로 갱신: orderId={}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("배송 요청 이벤트 역직렬화 실패: message={}", message, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
    @KafkaListener(topics = "order-service-topic", groupId = "order-service-delivery-group")
    public void handleDeliveryStartFailedEvent(String message){
        try{
            DeliveryStartFailedEvent event = objectMapper.readValue(message, DeliveryStartFailedEvent.class);
            log.info("배송 시작 실패 이벤트 수신: orderId={}", event.orderId());
            orderService.appendOrderStatus(event.orderId(), OrderStatus.FAILED);
            log.info("배송 시작 실패에 따라 주문 상태를 FAILED로 갱신: orderId={}", event.orderId());
        } 
        catch (JsonProcessingException e) {
            log.error("배송 시작 실패 이벤트 역직렬화 실패: message={}", message, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
}