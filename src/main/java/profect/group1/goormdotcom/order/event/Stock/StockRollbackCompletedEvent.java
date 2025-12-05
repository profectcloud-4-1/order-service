package profect.group1.goormdotcom.order.event.Stock;

import java.time.Instant;
import java.util.UUID;

/**
 * 재고 서비스에서 재고 롤백이 완료되었음을 알리는 이벤트.
 * 주문 서비스는 이 이벤트를 수신하여 주문 상태를 FAILED로 변경한다.
 */
public record StockRollbackCompletedEvent(
    UUID orderId,
    Instant occurredAt
) {
}
