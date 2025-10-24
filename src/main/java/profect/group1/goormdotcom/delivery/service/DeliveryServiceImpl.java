package profect.group1.goormdotcom.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryStepHistoryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnStepHistoryRepository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryEntity;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryMapper;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStatus;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStepType;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.domain.DeliveryReturn;
import profect.group1.goormdotcom.delivery.domain.DeliveryManager;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.controller.dto.request.CreateAddressRequestDto;


@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

	private final DeliveryRepository repo;
	private final DeliveryReturnRepository returnRepo;
	private final DeliveryAddressRepository addressRepo;
	private final DeliveryReturnAddressRepository returnAddressRepo;
	private final DeliveryStepHistoryRepository stepHistoryRepo;
	private final DeliveryReturnStepHistoryRepository returnStepHistoryRepo;

	private final DeliveryManager deliveryManager;

	public Integer canReturn(UUID orderId) {
		Optional<DeliveryEntity> found = this.repo.findByOrderId(orderId);
		if (found.isEmpty()) return -1;

		return DeliveryMapper.toDomain(found.get()).canReturn();
	}

	public Delivery createDelivery(UUID orderId, UUID customerAddressId) {
		Delivery delivery = this.deliveryManager.createDelivery(orderId, customerAddressId);

		return delivery;
	}

	public boolean cancel(UUID deliveryId) {
		this.deliveryManager.cancel(deliveryId);
		return true;
	}

	public DeliveryReturn returnDelivery(UUID deliveryId) {
		DeliveryReturn deliveryReturn = this.deliveryManager.returnDelivery(deliveryId);
		return deliveryReturn;
	}

	public List<DeliveryAddress> getAddressesByCustomerId(UUID customerId) {
		return this.deliveryManager.getAddressesByCustomerId(customerId);
	}

	public DeliveryAddress createCustomerAddress(UUID customerId, CreateAddressRequestDto body) {
		return this.deliveryManager.createCustomerAddress(customerId, body.getAddress(), body.getAddressDetail(), body.getZipcode(), body.getPhone(), body.getName());
	}

    public DeliveryAddress updateCustomerAddress(UUID customerId, UUID addressId, CreateAddressRequestDto body) {
        return this.deliveryManager.updateCustomerAddress(customerId, addressId, body.getAddress(), body.getAddressDetail(), body.getZipcode(), body.getPhone(), body.getName());
	}

    public boolean deleteCustomerAddress(UUID customerId, UUID addressId) {
        this.deliveryManager.deleteCustomerAddress(customerId, addressId);
        return true;
	}

    // Brand address (MASTER)
    public DeliveryAddress getGoormAddress() {
        return this.deliveryManager.getGoormAddress();
    }

    public DeliveryAddress createGoormAddress(CreateAddressRequestDto body) {
        return this.deliveryManager.createGoormAddress(body.getAddress(), body.getAddressDetail(), body.getZipcode(), body.getPhone(), body.getName());
    }

    public DeliveryAddress updateGoormAddress(CreateAddressRequestDto body) {
        return this.deliveryManager.updateGoormAddress(body.getAddress(), body.getAddressDetail(), body.getZipcode(), body.getPhone(), body.getName());
    }

}
