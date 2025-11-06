package profect.group1.goormdotcom.order.domain.event;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배송 시작 이벤트
 */
@Getter
@RequiredArgsConstructor
public class DeliveryStartedEvent {
    private final UUID orderId;
    private final UUID deliveryId;
    private final String status; // "STARTED" or "FAILED"
}


