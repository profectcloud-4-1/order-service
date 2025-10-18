package profect.group1.goormdotcom.settings.controller.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommonCodeListResponseDto {
    public static CommonCodeListResponseDto of(List<CommonCode> list) {
        return new CommonCodeListResponseDto(list);
    }

    @Schema(description = "공통 코드 목록")
    @NotBlank
    private List<CommonCode> list;
}