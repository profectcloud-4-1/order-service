package profect.group1.goormdotcom.category.controller.mapper;

import org.springframework.stereotype.Component;


import profect.group1.goormdotcom.category.controller.dto.CategoryResponseDto;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryNode;


@Component
public class CategoryDtoMapper {
    public static CategoryResponseDto toCategoryDto(CategoryNode categoryNode) {
        return CategoryResponseDto.from(categoryNode);
    }
    
}
