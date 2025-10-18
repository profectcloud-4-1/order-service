package profect.group1.goormdotcom.settings.controller.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class CommonCodeCreateResponseDto {

    public static CommonCodeCreateResponseDto of(CommonCode commonCode) {
        return new CommonCodeCreateResponseDto(commonCode.getCode());
    }

    @Schema(description = "생성된 공통 코드 ID")
    @NotBlank
    private String code;
}