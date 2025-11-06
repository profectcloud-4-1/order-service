package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kafka로 재고 차감 요청을 보내기 위한 메시지 DTO
 */
public record StockDecreaseRequestMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("products")
    List<ProductStockRequest> products
) {
    public record ProductStockRequest(
        @JsonProperty("productId")
        UUID productId,
        
        @JsonProperty("quantity")
        int quantity
    ) {}
}

