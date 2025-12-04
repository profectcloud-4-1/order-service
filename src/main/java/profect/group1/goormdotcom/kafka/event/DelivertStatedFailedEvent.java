package profect.group1.goormdotcom.kafka.event;

public class DelivertStatedFailedEvent {
    private UUID orderId;
    private String errorMessage;
    private Instant occurredAt;

    public DelivertStatedFailedEvent(UUID orderId, String errorMessage, Instant occurredAt) {
        this.orderId = orderId;
        this.errorMessage = errorMessage;
        this.occurredAt = occurredAt;
    }
}