package profect.group1.goormdotcom.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import profect.group1.goormdotcom.delivery.event.DeliveryStartedEvent;
import profect.group1.goormdotcom.delivery.event.DeliveryStartFailedEvent;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.repository.OrderRepository;
import profect.group1.goormdotcom.order.repository.OrderStatusRepository;
import profect.group1.goormdotcom.order.repository.entity.OrderEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderStatusEntity;

/**
 * 배송 서비스에서 발행한 이벤트를 수신해 주문 상태를 갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveryStatusEventListener {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    /**
     * 배송 서비스가 `DeliveryStartedEvent`를 발행했을 때 호출된다.
     * 배송 생성이 정상적으로 완료됐음을 의미하므로 주문 상태를 COMPLETED(배송 준비 완료)로 전환하고
     * 상태 이력을 남긴다.
     */
    @Async
    @Transactional // REQUIRES_NEW 없이도 동작하는지 확인
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    public void onDeliveryStarted(DeliveryStartedEvent event) {
        orderRepository.findById(event.orderId())
            .ifPresentOrElse(
                order -> handleStarted(order),
                () -> log.warn("배송 시작 이벤트 처리 실패 - 주문을 찾을 수 없음: orderId={}", event.orderId())
            );
    }

    private void handleStarted(OrderEntity order) {
        order.updateStatus(OrderStatus.COMPLETED);
        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(order)
                .status(OrderStatus.COMPLETED.getCode())
                .build()
        );
        log.info("배송 시작에 따라 주문 상태를 COMPLETED로 갱신: orderId={}", order.getId());
    }

    /**
     * 배송 서비스가 `DeliveryStartFailedEvent`를 발행했을 때 호출된다.
     * 배송 시작이 실패했음을 의미하므로 주문 상태를 FAILED로 전환하고 상태 이력을 남긴다.
     */
    @Async
    @Transactional
    @EventListener
    public void onDeliveryStartFailed(DeliveryStartFailedEvent event) {
        log.warn("배송 시작 실패 이벤트 수신: orderId={}, errorMessage={}", event.orderId(), event.errorMessage());
        orderRepository.findById(event.orderId())
            .ifPresentOrElse(
                order -> handleStartFailed(order),
                () -> log.warn("배송 시작 실패 이벤트 처리 실패 - 주문을 찾을 수 없음: orderId={}", event.orderId())
            );
    }

    private void handleStartFailed(OrderEntity order) {
        order.updateStatus(OrderStatus.FAILED);
        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(order)
                .status(OrderStatus.FAILED.getCode())
                .build()
        );
        log.info("배송 시작 실패에 따라 주문 상태를 CANCELED로 갱신: orderId={}", order.getId());
    }
}

