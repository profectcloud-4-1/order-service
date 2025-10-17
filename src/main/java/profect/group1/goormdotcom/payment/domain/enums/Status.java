package profect.group1.goormdotcom.payment.domain.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Status {
    PENDING("결제 대기"),
    SUCCESS("결제 승인"),
    FAIL("결제 실패"),
    CANCEL("결제 취소");

    private final String description;
}
