package profect.group1.goormdotcom.delivery.controller.dto.response;

import java.util.List;

import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class CustomerAddressListResponseDto {

    public static CustomerAddressListResponseDto of(List<DeliveryAddress> list) {
        return new CustomerAddressListResponseDto(list);
    }

    @Schema(description = "배송지 목록")
    private List<DeliveryAddress> list;
}