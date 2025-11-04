package profect.group1.goormdotcom.order.infrastructure.client.dto;

import java.util.UUID;

public record StockAdjustmentRequestItemDto(
    UUID productId,
    int requestedStockQuantity
) {
}

