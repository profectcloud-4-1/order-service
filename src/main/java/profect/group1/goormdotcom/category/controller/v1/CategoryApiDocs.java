package profect.group1.goormdotcom.category.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;

import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.category.controller.dto.CategoryResponseDto;

@Tag(name = "Category", description = "카테고리 API")
public interface CategoryApiDocs {

    @Operation(summary = "카테고리 트리 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "success", value = "{\"code\":\"COMMON200\",\"message\":\"성공입니다.\"}"))
    )
    ApiResponse<CategoryResponseDto> getCategory();
}
