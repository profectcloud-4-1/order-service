package profect.group1.goormdotcom.order.controller.v1;


import java.util.UUID;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import profect.group1.goormdotcom.order.controller.dto.OrderRequestDto;
import profect.group1.goormdotcom.order.service.OrderService;
import profect.group1.goormdotcom.order.domain.Order;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    //주문생성 (재고 확인까지)
    // *POST /api/v1/orders
    @PostMapping("api/v1/orders")
    public ResponseEntity<Order> create(@Valid @RequestBody OrderRequestDto req) {
        Order order = orderService.create(req);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable UUID orderId, @RequestParam UUID paymentId) {
        return ResponseEntity.ok(orderService.cancel(orderId, paymentId));
    }
    /**
     * 반송 완료 처리 (배송 서비스에서 webhook 호출)
     * POST /api/v1/orders/{orderId}/return/complete
     */
    // @PostMapping("/{orderId}/return/complete")
    // public ResponseEntity<OrderResponseDto> completeReturn(@PathVariable UUID orderId) {
    //     return ResponseEntity.ok(orderService.completeReturn(orderId));
    // }
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(){
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getOne(id));
    }

}
