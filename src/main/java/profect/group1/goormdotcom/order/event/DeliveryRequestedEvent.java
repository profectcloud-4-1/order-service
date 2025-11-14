package profect.group1.goormdotcom.order.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 주문 결제 완료 후 배송 서비스를 호출하기 위한 도메인 이벤트.
 */
public record DeliveryRequestedEvent(
        UUID orderId,
        UUID customerId,
        String address,
        String addressDetail,
        String zipcode,
        String phone,
        String name,
        String deliveryMemo,
        Instant occurredAt
) {
}

