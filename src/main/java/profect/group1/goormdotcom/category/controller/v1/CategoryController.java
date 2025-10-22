package profect.group1.goormdotcom.category.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;
import profect.group1.goormdotcom.category.controller.dto.CategoryRequestDto;
import profect.group1.goormdotcom.category.controller.dto.CategoryResponseDto;
import profect.group1.goormdotcom.category.controller.dto.CategoryTreeResponseDto;
import profect.group1.goormdotcom.category.controller.mapper.CategoryDtoMapper;
import profect.group1.goormdotcom.category.domain.Category;
import profect.group1.goormdotcom.category.service.CategoryService;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryTree;
import profect.group1.goormdotcom.category.service.CategoryTree.CategroyTreeService;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryController implements CategoryApiDocs{
    private final CategoryService categoryService;
    private final CategroyTreeService categoryTreeService;

    @GetMapping
    public ApiResponse<CategoryTreeResponseDto> getCategory() {
        CategoryTree categoryTree = categoryTreeService.getAllCategoryTree();
        CategoryTreeResponseDto categoryResponseDto = CategoryDtoMapper.toCategoryTreeDto(categoryTree.root());
        return ApiResponse.of(SuccessStatus._OK, categoryResponseDto);
    }

    @GetMapping("{categoryId}")
    public ApiResponse<CategoryTreeResponseDto> getChildCategory(
        @PathVariable(value = "categoryId") UUID categoryId
    ) {
        CategoryTree categoryTree = categoryTreeService.getChildCategoryTree(categoryId);
        CategoryTreeResponseDto categoryResponseDto = CategoryDtoMapper.toCategoryTreeDto(categoryTree.root());
        return ApiResponse.of(SuccessStatus._OK, categoryResponseDto);
    }

    @PostMapping
    public ApiResponse<UUID> createCategory(
        @RequestBody @Valid CategoryRequestDto categoryRequestDto
    ) {
        
        UUID id = categoryService.createCategory(categoryRequestDto.name(), categoryRequestDto.parentId());
        return ApiResponse.of(SuccessStatus._OK, id);
    }
    
    @DeleteMapping("/{categoryId}")
    public ApiResponse<UUID> deleteCategory(
        @PathVariable(value = "categoryId") UUID categoryId
    ) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.of(SuccessStatus._OK, categoryId);
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDto> updateCategory(
        @PathVariable(value = "categoryId") UUID categoryId,
        @RequestBody @Valid CategoryRequestDto categoryRequestDto
    ) {
        Category category = categoryService.updateCategory(categoryId, categoryRequestDto.parentId(), categoryRequestDto.name());
        return ApiResponse.of(SuccessStatus._OK, CategoryDtoMapper.toCategoryDto(category));
    }
}
