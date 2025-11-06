package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 배송 시작 메시지 (Delivery -> Order)
 */
public record DeliveryStartMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("deliveryId")
    UUID deliveryId,
    
    @JsonProperty("status")
    String status // "STARTED" or "FAILED"
) {}

