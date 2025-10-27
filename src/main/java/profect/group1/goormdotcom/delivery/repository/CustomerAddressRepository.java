package profect.group1.goormdotcom.delivery.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import profect.group1.goormdotcom.delivery.repository.entity.CustomerAddressEntity;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddressEntity, UUID> {
	Optional<CustomerAddressEntity> findByCustomerId(UUID customerId);
    List<CustomerAddressEntity> findAllByCustomerId(UUID customerId);
}