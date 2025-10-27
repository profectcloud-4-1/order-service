package profect.group1.goormdotcom.product.controller.dto;

import java.util.List;
import java.util.UUID;

public record DeleteProductRequestDto(
    List<UUID> productIds,
    UUID brandId
) {
    
}
