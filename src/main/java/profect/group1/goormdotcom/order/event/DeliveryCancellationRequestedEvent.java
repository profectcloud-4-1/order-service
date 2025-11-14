package profect.group1.goormdotcom.order.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 결제 실패 등으로 배송 취소가 필요한 경우 발행되는 도메인 이벤트.
 */
public record DeliveryCancellationRequestedEvent(
        UUID orderId,
        Instant occurredAt
) {
}

