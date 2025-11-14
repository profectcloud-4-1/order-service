package profect.group1.goormdotcom.delivery.event;

import profect.group1.goormdotcom.order.event.DeliveryCancellationRequestedEvent;
import profect.group1.goormdotcom.order.event.DeliveryRequestedEvent;

/**
 * 배송 서비스가 처리해야 하는 주문발 배송 이벤트 계약.
 */
public interface DeliveryEventHandlerInterface {

    void onDeliveryRequested(DeliveryRequestedEvent event);

    void onDeliveryCancellationRequested(DeliveryCancellationRequestedEvent event);
}

