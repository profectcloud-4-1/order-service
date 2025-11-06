package profect.group1.goormdotcom.order.domain.event;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 완료 이벤트
 */
@Getter
@RequiredArgsConstructor
public class PaymentCompletedEvent {
    private final UUID orderId;
    private final String paymentKey;
    private final Integer amount;
    private final String status; // "SUCCESS" or "FAILED"
}


