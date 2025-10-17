package profect.group1.goormdotcom.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.payment.domain.enums.PayType;
import profect.group1.goormdotcom.payment.domain.enums.Status;
import profect.group1.goormdotcom.payment.repository.entity.PaymentEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private UUID id;
    private UUID orderId;
    private PayType payType;
    private Status status;
    private Long amount;
    private String paymentKey;
    private String failReason;
    private boolean isCancelled;
    private String cancelReason;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;

    public Payment(
            UUID orderId,
            PayType payType,
            Long amount
    ) {
        this.orderId = orderId;
        this.payType = payType;
        this.amount = amount;
        this.status = Status.PENDING;
        this.isCancelled = false;
    }

    public static Payment create(UUID orderId,
                                       PayType payType,
                                       Long amount) {
        return new Payment(orderId, payType, amount);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void markSuccess(String paymentKey, LocalDateTime approvedAt) {
        this.status = Status.SUCCESS;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
    }

    public void markCancel(String reason, LocalDateTime cancelledAt) {
        this.status = Status.CANCEL;
        this.isCancelled = true;
        this.cancelReason = reason;
        this.cancelledAt = cancelledAt;
    }

    public void markFail(String reason) {
        this.status = Status.FAIL;
        this.cancelReason = reason;
    }
}
