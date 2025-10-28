package profect.group1.goormdotcom.delivery.controller.v1.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.delivery.controller.dto.request.CreateAddressRequestDto;
import profect.group1.goormdotcom.delivery.controller.dto.response.CustomerAddressListResponseDto;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.controller.dto.request.CreateDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.controller.dto.request.CancelDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.controller.dto.request.StartDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.controller.dto.response.DeliveryResponseDto;

@Tag(name = "Delivery (external)", description = "배송 API (외부 클라이언트 노출용)")
public interface DeliveryApiDocs {

    @Operation(summary = "배송 정보 조회", description = "주문 ID를 기반으로 배송 정보를 조회합니다.")
    ApiResponse<DeliveryResponseDto> getDeliveryByOrder(@RequestParam UUID orderId);

    @Operation(summary = "내 배송지 목록 조회", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<CustomerAddressListResponseDto> getMyAddresses(HttpServletRequest request);

    @Operation(summary = "내 배송지 생성", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<DeliveryAddress> createMyAddress(@RequestBody CreateAddressRequestDto body, HttpServletRequest request);

    @Operation(summary = "내 배송지 수정", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<DeliveryAddress> updateMyAddress(
        @RequestBody CreateAddressRequestDto body,
        HttpServletRequest request,
        @PathVariable @Parameter(description = "주소 ID") UUID addressId
    );

    @Operation(summary = "내 배송지 삭제", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<Boolean> deleteMyAddress(HttpServletRequest request, @PathVariable @Parameter(description = "주소 ID") UUID addressId);

    @Operation(summary = "구름닷컴 배송지 조회", description = "MASTER only", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<DeliveryAddress> getGoormAddress();

    @Operation(summary = "구름닷컴 배송지 생성", description = "MASTER only", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<DeliveryAddress> createGoormAddress(@RequestBody CreateAddressRequestDto body);

    @Operation(summary = "구름닷컴 배송지 수정", description = "MASTER only", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponse<DeliveryAddress> updateGoormAddress(
        @RequestBody CreateAddressRequestDto body
    );
}
