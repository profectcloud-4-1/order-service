package profect.group1.goormdotcom.review.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderClientFallback implements OrderClient {
    
    @Override
    public UUID getOrderIdByUserAndProduct(UUID customerId, UUID productId) {
        log.error("[Feign-Fallback] Order-service request failed. customerId: {}, productId: {}", 
                  customerId, productId);
        throw new RuntimeException("주문 서비스가 일시적으로 사용할 수 없습니다.");
    }
}

