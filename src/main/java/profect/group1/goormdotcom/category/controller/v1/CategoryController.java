package profect.group1.goormdotcom.category.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;
import profect.group1.goormdotcom.category.controller.dto.CategoryResponseDto;
import profect.group1.goormdotcom.category.controller.mapper.CategoryDtoMapper;
import profect.group1.goormdotcom.category.service.CategoryService;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryTree;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryController implements CategoryApiDocs{
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<CategoryResponseDto> getCategory() {
        CategoryTree categoryTree = categoryService.getCategoryTree();
        CategoryResponseDto categoryResponseDto = CategoryDtoMapper.toCategoryDto(categoryTree.root());
        return ApiResponse.of(SuccessStatus._OK, categoryResponseDto);
    }
    
}
