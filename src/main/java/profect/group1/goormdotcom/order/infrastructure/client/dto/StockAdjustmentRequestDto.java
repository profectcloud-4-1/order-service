package profect.group1.goormdotcom.order.infrastructure.client.dto;

import java.util.List;

public record StockAdjustmentRequestDto(
    List<StockAdjustmentRequestItemDto> products
) {
}

