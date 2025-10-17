package profect.group1.goormdotcom.payment.repository.mapper;

import org.springframework.stereotype.Component;
import profect.group1.goormdotcom.payment.domain.Payment;
import profect.group1.goormdotcom.payment.domain.enums.PayType;
import profect.group1.goormdotcom.payment.domain.enums.Status;
import profect.group1.goormdotcom.payment.repository.entity.PaymentEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentMapper {
    public static Payment toDomain(
            final PaymentEntity entity
    ) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getPayType(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getPaymentKey(),
                entity.getFailReason(),
                entity.isCancelled(),
                entity.getCancelReason(),
                entity.getApprovedAt(),
                entity.getCancelledAt()
                //TODO: createdAt, updatedAt 넣기
        );
    }

    public static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
                payment.getOrderId(),
                payment.getPayType(),
                payment.getAmount()
        );
    }

}
