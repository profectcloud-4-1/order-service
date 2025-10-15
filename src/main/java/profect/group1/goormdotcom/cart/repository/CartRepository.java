package profect.group1.goormdotcom.cart.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import profect.group1.goormdotcom.cart.repository.entity.CartEntity;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

	CartEntity getCartEntityByCustomerId(UUID customerId);
}
