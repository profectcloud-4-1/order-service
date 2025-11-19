package profect.group1.goormdotcom.order.controller.internal.v1;

import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.order.service.OrderService;
import profect.group1.goormdotcom.order.domain.Order;

@RestController
@RequestMapping("/internal/v1/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    //결제 완료
    // 부하 테스트를 위한 주문 orderId 랜덤으로 받음
    //orderservice에 부하테스트를 위한 주문 생성 메서드(createOrderForLoadTest)가 제일 아레에 존재 
    @PostMapping("/payment/success")
    // public ApiResponse<Order> completePayment(@PathVariable UUID orderId) {
    public ApiResponse<Order> completePayment() {
        UUID orderId = UUID.randomUUID();
        return ApiResponse.onSuccess(orderService.createOrderForLoadTest());
    }
    //원래 코드
    //  @PostMapping("/{orderId}/payment/success")
    // public ApiResponse<Order> completePayment(@PathVariable UUID orderId) {
    //     return ApiResponse.onSuccess(orderService.completePayment(orderId));
    // }

    //결제 실패
    //재고 원복
    //주문 상태 실패로 변경
    //주문 히스토리 저장
    @PostMapping("/{orderId}/payment/fail")
    public ApiResponse<Order> failPayment(@PathVariable UUID orderId) {
        return ApiResponse.onSuccess(orderService.failPayment(orderId));
    }

    //반송 완료
    @PostMapping("/api/v1/orders/{orderId}/return-completed")
    public ApiResponse<Boolean> deliveryReturnCompleted(@PathVariable UUID orderId) {
        orderService.deliveryReturnCompleted(orderId);
        return ApiResponse.onSuccess(Boolean.TRUE);
    }

}
