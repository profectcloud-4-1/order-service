package profect.group1.goormdotcom.payment.controller.dto;

import lombok.Builder;
import lombok.Getter;
import profect.group1.goormdotcom.payment.domain.enums.PayType;
import profect.group1.goormdotcom.payment.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;


public record PaymentResponseDto (
        UUID id,
        UUID orderId,
        PayType payType,
        Status status,
        Long amount
) {

}
