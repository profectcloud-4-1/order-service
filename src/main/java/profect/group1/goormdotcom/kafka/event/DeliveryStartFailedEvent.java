package profect.group1.goormdotcom.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryStartFailedEvent(
    UUID orderId,
    String errorMessage,
    Instant occurredAt
) {
}

