package profect.group1.goormdotcom.settings.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommonCodeRequestDto {

    @NotNull(message = "코드는 필수입니다")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{4}$", message = "대문자 3자리 + 숫자 4자리로 구성되어야 합니다.")
    @Schema(description = "공통 코드. 대문자 3자리 + 숫자 4자리.", example = "DEL0001")
    private String code;

    @NotNull(message = "그룹은 필수입니다")
    @Schema(description = "공통 코드 그룹. 허용값: 'DELIVERY', 'ORDER', 'PAYMENT', 'PRODUCT', 'REVIEW', 'USER', 'COMMON', 'SETTING', 'OTHER'.", example = "DELIVERY")
    private String groupName;

    @NotNull(message = "라벨은 필수입니다")
    @Schema(description = "라벨. 예: '배송중', '주문완료', '결제실패' 등.", example = "배송중")
    private String label;

    @NotNull(message = "설명은 필수입니다")
    @Schema(description = "해당 코드에 대한 설명", example = "구매자에게 배송중")
    private String description;
}
