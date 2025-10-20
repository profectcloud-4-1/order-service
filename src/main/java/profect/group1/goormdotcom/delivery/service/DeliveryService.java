package profect.group1.goormdotcom.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepository deliveryRepo;
	private final DeliveryReturnRepository deliveryReturnRepo;
}
