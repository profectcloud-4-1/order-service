package profect.group1.goormdotcom.category.controller.mapper;

import org.springframework.stereotype.Component;

import profect.group1.goormdotcom.category.controller.dto.CategoryResponseDto;
import profect.group1.goormdotcom.category.controller.dto.CategoryTreeResponseDto;
import profect.group1.goormdotcom.category.domain.Category;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryNode;


@Component
public class CategoryDtoMapper {
    public static CategoryTreeResponseDto toCategoryTreeDto(CategoryNode categoryNode) {
        return CategoryTreeResponseDto.from(categoryNode);
    }
    
    public static CategoryResponseDto toCategoryDto(Category category) {
        return new CategoryResponseDto(category.getId(), category.getParentId(), category.getName());
    }
}
