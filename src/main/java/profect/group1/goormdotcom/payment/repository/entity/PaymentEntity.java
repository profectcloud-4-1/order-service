package profect.group1.goormdotcom.payment.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import profect.group1.goormdotcom.payment.domain.enums.PayType;
import profect.group1.goormdotcom.payment.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

//TODO: BaseEntity 상속받기
@Entity
@Table(name = "p_payment")
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 32)
    private PayType payType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "payment_key", length = 200, nullable = true)
    private String paymentKey;

    @Column(name = "fail_reason", nullable = true)
    private String failReason;

    @Column(name = "is_cancelled", nullable = false)
    private boolean isCancelled;

    @Column(name = "cancel_reason", nullable = true)
    private String cancelReason;

    @Column(name = "approved_at", nullable = true)
    private LocalDateTime approvedAt;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDateTime cancelledAt;

    public PaymentEntity(final UUID orderId,
                         final PayType payType,
                         final Long amount) {
        this.orderId = orderId;
        this.payType = payType;
        this.amount = amount;
        this.status = Status.PENDING;
        this.isCancelled = false;
    }
}
