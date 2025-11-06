package profect.group1.goormdotcom.order.infrastructure.kafka.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.order.domain.event.PaymentCompletedEvent;
import profect.group1.goormdotcom.order.infrastructure.event.OrderEventPublisher;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.PaymentSuccessMessage;

/**
 * 결제 완료 메시지를 수신하는 Consumer (Payment -> Order)
 * Kafka 메시지를 도메인 이벤트로 변환하여 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessConsumer {

    private static final String TOPIC = "payment-success-topic";
    private static final String GROUP_ID = "order-group";

    private final OrderEventPublisher eventPublisher;

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID, containerFactory = "paymentKafkaListenerContainerFactory")
    public void consumePaymentSuccess(PaymentSuccessMessage message) {
        log.info("결제 완료 메시지 수신: orderId={}, paymentKey={}, status={}", 
            message.orderId(), message.paymentKey(), message.status());

        try {
            // Kafka 메시지를 도메인 이벤트로 변환하여 발행
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                message.orderId(),
                message.paymentKey(),
                message.amount(),
                message.status()
            );
            
            eventPublisher.publishPaymentCompleted(event);
            log.info("결제 완료 이벤트 발행 완료: orderId={}", message.orderId());
        } catch (Exception e) {
            log.error("결제 완료 메시지 처리 실패: orderId={}, error={}", 
                message.orderId(), e.getMessage(), e);
            // TODO: 실패 시 재시도 로직 또는 Dead Letter Queue 처리
        }
    }
}

