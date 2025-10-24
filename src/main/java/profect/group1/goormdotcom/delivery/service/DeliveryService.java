package profect.group1.goormdotcom.delivery.service;

import java.util.UUID;
import java.util.List;

import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.domain.DeliveryReturn;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.controller.dto.request.CreateAddressRequestDto;

public interface DeliveryService {
	/**
	 * 반송 가능 여부 확인
	 * @return -1: 존재하지 않는 order id, 0: 반송불가(현재 배송중), 1: 즉시취소 가능(배송 시작 전이기 때문), 2: 반송 프로세스를 거쳐 반송 가능(배송 완료됨)
	 */
	public Integer canReturn(UUID orderId);
	/** 
	 * 배송 취소 (즉시취소 가능한 경우만 사용, 불가능한 경우 예외 발생)
	 * @return 정상처리여부
	 */
	public boolean cancel(UUID deliveryId);
	/** 
	 * 배송 생성
	 * @param orderId 주문 ID
	 * @param customerAddressId 고객 배송지 ID (p_customer_address.id)
	 * @return 배송 정보
	 */
	public Delivery createDelivery(UUID orderId, UUID customerAddressId);

	/** 
	 * 배송 반송 (배송 완료된 경우만 사용, 배송완료가 아닌 경우 예외 발생)
	 * @param deliveryId 배송 ID
	 * @return 정상처리여부
	 */
	public DeliveryReturn returnDelivery(UUID deliveryId);

	/**
	 * 고객 배송지 목록 조회
	 * @param customerId 고객 ID
	 * @return 배송지 목록
	 */
	public List<DeliveryAddress> getAddressesByCustomerId(UUID customerId);
	public DeliveryAddress createCustomerAddress(UUID customerId, CreateAddressRequestDto body);
	public DeliveryAddress updateCustomerAddress(UUID customerId, UUID addressId, CreateAddressRequestDto body);
	public boolean deleteCustomerAddress(UUID customerId, UUID addressId);

    // Goorm address (MASTER)
    public DeliveryAddress getGoormAddress();
    public DeliveryAddress createGoormAddress(CreateAddressRequestDto body);
	public DeliveryAddress updateGoormAddress(CreateAddressRequestDto body);

	public Delivery getDeliveryByOrderId(UUID orderId);
}
