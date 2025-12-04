package profect.group1.goormdotcom.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryStartedEvent(
    UUID orderId,
    UUID deliveryId,
    Instant occurredAt
) {
}
