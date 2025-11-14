package profect.group1.goormdotcom.order.domain.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@RequiredArgsConstructor
@Schema(description = "주문 상태")
public enum OrderStatus {
    PENDING("ORD0001", "대기"),
    PAID("ORD0002", "결제완료"),
    COMPLETED("ORD0003", "완료"),
    CANCELLED("ORD0004", "취소"),
    FAILED("ORD0005", "실패");

    private final String code;
    private final String label;

    // DELETED

    public static OrderStatus fromCode(String code) {
        return Arrays.stream(OrderStatus.values())
            .filter(status -> status.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid order status code: " + code));
    }

}
