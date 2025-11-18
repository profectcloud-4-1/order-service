package profect.group1.goormdotcom.delivery.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * 배송 시작 실패를 알리는 보상 이벤트.
 * 주문 서비스는 이 이벤트를 수신하여 주문 상태를 FAIL 또는 CANCEL로 변경한다.
 */
@Builder
public record DeliveryStartFailedEvent(
    UUID orderId,
    String errorMessage,
    Instant occurredAt
) {
}

