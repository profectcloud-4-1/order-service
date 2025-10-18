package profect.group1.goormdotcom.settings.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditCommonCodeRequestDto {

    @Schema(description = "공통 코드 그룹. 허용값: 'DELIVERY', 'ORDER', 'PAYMENT', 'PRODUCT', 'REVIEW', 'USER', 'COMMON', 'SETTING', 'OTHER'.", example = "DELIVERY")
    private String groupName;
    @Schema(description = "라벨. 예: '배송중', '주문완료', '결제실패' 등.", example = "배송완료")
    private String label;
    @Schema(description = "해당 코드에 대한 설명", example = "구매자에게 배송완료")
    private String description;
}
