package profect.group1.goormdotcom.settings.controller.v1;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import java.util.List;
import profect.group1.goormdotcom.settings.service.SettingService;
import profect.group1.goormdotcom.settings.controller.dto.request.CreateCommonCodeRequestDto;
import profect.group1.goormdotcom.settings.controller.dto.request.EditCommonCodeRequestDto;
import profect.group1.goormdotcom.settings.controller.dto.response.CommonCodeCreateResponseDto;
import profect.group1.goormdotcom.settings.controller.dto.response.CommonCodeListResponseDto;
import profect.group1.goormdotcom.apiPayload.code.status.ErrorStatus;

@RestController
@RequestMapping("/settings")
public class SettingsController implements SettingsApiDocs {

	private final SettingService settingService;

	public SettingsController(SettingService settingService) {
		this.settingService = settingService;
	}

    @PostMapping("/common-codes")
    public ApiResponse<CommonCodeCreateResponseDto> createCommonCode(
        @RequestBody @Valid CreateCommonCodeRequestDto body
    ) {
        CommonCode commonCode = settingService.createCommonCode(body.getCode(), body.getGroupName(), body.getLabel(), body.getDescription());
        return ApiResponse.of(SuccessStatus._OK, CommonCodeCreateResponseDto.of(commonCode));
    }

    @GetMapping("/common-codes/{groupName}")
    public ApiResponse<CommonCodeListResponseDto> getCommonCodeByGroup(
		@PathVariable String groupName
	) {
		List<CommonCode> list = settingService.findCommonCodeByGroup(groupName);
		return ApiResponse.onSuccess(CommonCodeListResponseDto.of(list));
	}
	
    @PatchMapping("/common-codes/{code}")
    public ApiResponse<Void> updateCommonCode(
        @PathVariable String code,
        @RequestBody @Valid EditCommonCodeRequestDto body
    ) {
        settingService.updateCommonCode(code, body.getGroupName(), body.getLabel(), body.getDescription());
        return ApiResponse.onSuccess(null);
    }
	
}
