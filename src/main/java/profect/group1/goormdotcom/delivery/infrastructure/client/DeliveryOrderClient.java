package profect.group1.goormdotcom.delivery.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;
import java.lang.Boolean;
import org.springframework.web.bind.annotation.PathVariable;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;


@FeignClient(
		name = "delivery-to-order",
        url = "${service.order.url}",
		fallback = DeliveryOrderClientFallBack.class
)
public interface DeliveryOrderClient {
	@PostMapping("/api/v1/orders/{orderId}/return-completed")
	ApiResponse<Boolean> onReturnCompleted(@PathVariable UUID orderId);
}
