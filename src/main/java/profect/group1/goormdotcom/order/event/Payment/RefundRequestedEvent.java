package profect.group1.goormdotcom.order.event.Payment;

import java.time.Instant;
import java.util.UUID;

/**
 * 주문 취소 시 결제 서비스에 환불을 요청하기 위한 도메인 이벤트.
 */
public record RefundRequestedEvent(
        UUID orderId,
        int refundAmount,
        String reason,
        Instant occurredAt
) {
}

