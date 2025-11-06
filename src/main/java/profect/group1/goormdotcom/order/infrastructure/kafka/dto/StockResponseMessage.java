package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 재고 확인 응답 메시지 (Stock -> Order)
 */
public record StockResponseMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("productId")
    UUID productId,
    
    @JsonProperty("status")
    String status, // "SUCCESS" or "FAILED"
    
    @JsonProperty("message")
    String message
) {}

