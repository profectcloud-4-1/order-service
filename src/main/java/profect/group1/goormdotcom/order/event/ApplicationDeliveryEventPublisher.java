package profect.group1.goormdotcom.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Spring {@link ApplicationEventPublisher}를 이용한 배송 이벤트 발행 구현체.
 */
@Component
@RequiredArgsConstructor
public class ApplicationDeliveryEventPublisher implements DeliveryEventPublisherInterface {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Async
    public void publishDeliveryRequested(DeliveryRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Async
    public void publishDeliveryCancellationRequested(DeliveryCancellationRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

