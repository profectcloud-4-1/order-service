package profect.group1.goormdotcom.delivery.controller.internal.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.delivery.controller.internal.v1.request.CancelDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.controller.internal.v1.request.StartDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.domain.Delivery;

@Tag(name = "Delivery (internal)", description = "배송 API (내부서비스간 통신용)")
public interface DeliveryInternalApiDocs {

    @Operation(summary = "반송 가능 여부 확인")
    ApiResponse<Integer> checkCancellable(@RequestParam UUID orderId);

    @Operation(summary = "배송 시작 요청")
    ApiResponse<Delivery> startDelivery(@RequestBody StartDeliveryRequestDto body);

    @Operation(summary = "배송 취소")
    ApiResponse<Object> cancelDelivery(@RequestBody CancelDeliveryRequestDto body);

    @Operation(summary = "반송 요청")
    ApiResponse<Object> returnDelivery(@RequestBody CancelDeliveryRequestDto body);

    
}
