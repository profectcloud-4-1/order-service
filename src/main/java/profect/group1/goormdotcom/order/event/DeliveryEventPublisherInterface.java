package profect.group1.goormdotcom.order.event;

/**
 * 주문 서비스에서 배송 관련 이벤트를 발행하기 위한 추상화.
 */
public interface DeliveryEventPublisherInterface {

    void publishDeliveryRequested(DeliveryRequestedEvent event);

    void publishDeliveryCancellationRequested(DeliveryCancellationRequestedEvent event);
}

