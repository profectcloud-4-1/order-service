package profect.group1.goormdotcom.order.domain.event;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 재고 차감 요청 이벤트
 */
@Getter
@RequiredArgsConstructor
public class StockDecreaseRequestedEvent {
    private final UUID orderId;
    private final List<ProductItem> products;
    
    @Getter
    @RequiredArgsConstructor
    public static class ProductItem {
        private final UUID productId;
        private final int quantity;
    }
}


