package profect.group1.goormdotcom.payment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import profect.group1.goormdotcom.apiPayload.code.status.ErrorStatus;
import profect.group1.goormdotcom.apiPayload.exceptions.handler.PaymentHandler;
import profect.group1.goormdotcom.payment.controller.dto.request.PaymentCreateRequestDto;
import profect.group1.goormdotcom.payment.domain.Payment;
import profect.group1.goormdotcom.payment.domain.enums.Status;
import profect.group1.goormdotcom.payment.repository.PaymentRepository;
import profect.group1.goormdotcom.payment.repository.entity.PaymentEntity;
import profect.group1.goormdotcom.payment.repository.mapper.PaymentMapper;
import profect.group1.goormdotcom.user.domain.User;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Payment requestPayment(PaymentCreateRequestDto dto, User user) {
        //TODO: order에서 orderId 존재하는지 확인? MSA에서는 처리할 것인지 고민

        //처리중인 결제내역이 있는지 확인
        if (dto.getOrderId() != null) {
            paymentRepository.findByOrderIdAndStatus(dto.getOrderId(), Status.PENDING)
                    .ifPresent(existing -> {
                        throw new PaymentHandler(ErrorStatus._DUPLICATE_REQUEST);
                    });
        }

        Payment payment = Payment.create(dto.getOrderId(), dto.getPayType(), dto.getAmount());

        //1000원 이하면 결제X
        if(payment.getAmount() < 1000) {
            throw new PaymentHandler(ErrorStatus._BAD_REQUEST);
        }

        PaymentEntity savedEntity = paymentRepository.save(PaymentMapper.toEntity(payment));
        payment.setId(savedEntity.getId());

        return payment;
    }

}
