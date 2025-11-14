package profect.group1.goormdotcom.delivery.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * 배송이 정상적으로 생성되어 시작 준비가 완료되었음을 알리는 이벤트.
 * 주문 서비스는 이 이벤트를 수신하여 주문 상태를 COMPLETED로 갱신한다.
 */
@Builder
public record DeliveryStartedEvent(
    UUID orderId,
    UUID deliveryId,
    Instant occurredAt
) {
}

