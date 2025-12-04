package profect.group1.goormdotcom.order.event.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Spring {@link ApplicationEventPublisher}를 이용한 결제 이벤트 발행 구현체.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher implements PaymentEventPublisherInterface {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Async
    public void publishRefundRequested(RefundRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

