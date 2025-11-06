package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 주문 완료 메시지 (Order -> Client)
 */
public record OrderCompletedMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("customerId")
    UUID customerId,
    
    @JsonProperty("status")
    String status, // "COMPLETED"
    
    @JsonProperty("totalAmount")
    Integer totalAmount,
    
    @JsonProperty("orderName")
    String orderName
) {}

