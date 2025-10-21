package profect.group1.goormdotcom.category.controller.dto;

import java.util.List;
import java.util.UUID;

import profect.group1.goormdotcom.category.service.CategoryTree.CategoryNode;

public record CategoryResponseDto(
    UUID id,
    String name,
    List<CategoryResponseDto> children
) {
    public static CategoryResponseDto from(CategoryNode node) {
        List<CategoryResponseDto> childDtos = node.children().stream().map(CategoryResponseDto::from).toList();
        return new CategoryResponseDto(
            node.id(), 
            node.name(), 
            childDtos
        );
    }
}
