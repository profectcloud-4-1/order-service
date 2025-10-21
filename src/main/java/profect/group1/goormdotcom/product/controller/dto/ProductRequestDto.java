package profect.group1.goormdotcom.product.controller.dto;

import java.util.List;
import java.util.UUID;

public record ProductRequestDto(
    String name,
    UUID brandId,
    UUID categoryId,
    String description,
    int price,
    int stockQuantity,
    List<UUID> imageIds
) {    
}
