package profect.group1.goormdotcom.delivery.controller.v1.internal;

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
import java.lang.Boolean;

@Tag(name = "Delivery (internal)", description = "배송 API (내부서비스간 통신용)")
public interface DeliveryInternalApiDocs {

    @Operation(summary = "반송 가능 여부 확인", description = "반송 가능 여부를 확인합니다.")
    ApiResponse<Integer> checkCancellable(@RequestParam UUID orderId);

    @Operation(summary = "배송 생성")
    ApiResponse<Delivery> createDelivery(@RequestBody CreateDeliveryRequestDto body);

    @Operation(summary = "배송 삭제", description = "결제 실패 등의 주문 전체 트랜잭션 실패 시 호출하는 보상용 api. 배송이 시작되지 않은 경우만 삭제 가능.")
    ApiResponse<Boolean> deleteDelivery(@RequestBody CreateDeliveryRequestDto body);

    @Operation(summary = "배송 시작 요청")
    ApiResponse<Delivery> startDelivery(@RequestBody StartDeliveryRequestDto body);

    @Operation(summary = "배송 취소")
    ApiResponse<Object> cancelDelivery(@RequestBody CancelDeliveryRequestDto body);

    @Operation(summary = "반송 요청")
    ApiResponse<Object> returnDelivery(@RequestBody CancelDeliveryRequestDto body);

    
}
