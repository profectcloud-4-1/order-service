package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 결제 완료 메시지 (Payment -> Order)
 */
public record PaymentSuccessMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("paymentKey")
    String paymentKey,
    
    @JsonProperty("amount")
    Integer amount,
    
    @JsonProperty("status")
    String status // "SUCCESS" or "FAILED"
) {}

