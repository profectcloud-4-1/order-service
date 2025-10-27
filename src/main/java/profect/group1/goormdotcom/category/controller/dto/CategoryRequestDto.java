package profect.group1.goormdotcom.category.controller.dto;

import java.util.UUID;

public record CategoryRequestDto(
    String name,
    UUID parentId
) {
} 
