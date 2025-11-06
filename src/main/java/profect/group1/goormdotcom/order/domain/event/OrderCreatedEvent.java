package profect.group1.goormdotcom.order.domain.event;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성 이벤트
 */
@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {
    private final UUID orderId;
    private final UUID customerId;
    private final List<ProductItem> products;
    
    @Getter
    @RequiredArgsConstructor
    public static class ProductItem {
        private final UUID productId;
        private final int quantity;
    }
}


