package profect.group1.goormdotcom.settings.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;	
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import profect.group1.goormdotcom.settings.controller.dto.request.CreateCommonCodeRequestDto;
import profect.group1.goormdotcom.settings.controller.dto.request.EditCommonCodeRequestDto;
import profect.group1.goormdotcom.settings.controller.dto.response.CommonCodeCreateResponseDto;
import profect.group1.goormdotcom.settings.controller.dto.response.CommonCodeListResponseDto;
import profect.group1.goormdotcom.apiPayload.ApiResponse;

@Tag(name = "Settings", description = "설정 관리 API")
public interface SettingsApiDocs {

	@Operation(
			summary = "공통 코드 생성",
			description = "새로운 공통 코드를 생성합니다."
	)
	ApiResponse<CommonCodeCreateResponseDto> createCommonCode(
			@Parameter(description = "생성할 공통 코드 요청 바디", required = true)
			@RequestBody @Valid CreateCommonCodeRequestDto body
	);

	@Operation(
			summary = "그룹별 공통 코드 조회",
			description = "그룹명으로 공통 코드 목록을 조회합니다."
	)
	ApiResponse<CommonCodeListResponseDto> getCommonCodeByGroup(
			@Parameter(description = "조회할 그룹명", required = true)
			@PathVariable("groupName") String groupName
	);

	@Operation(
			summary = "공통 코드 수정",
			description = "코드로 공통 코드를 부분 수정합니다."
	)
	ApiResponse<Void> updateCommonCode(
			@Parameter(description = "수정할 코드값", required = true)
			@PathVariable("code") String code,
			@Parameter(description = "수정할 필드들", required = true)
			@RequestBody @Valid EditCommonCodeRequestDto body
	);

}
