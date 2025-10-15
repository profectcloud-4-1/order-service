package profect.group1.goormdotcom.cart.repository.mapper;

import org.springframework.stereotype.Component;
import profect.group1.goormdotcom.cart.domain.CartItem;
import profect.group1.goormdotcom.cart.repository.entity.CartItemEntity;

@Component
public class CartItemMapper {

	public static CartItem toDomain(final CartItemEntity entity) {
		return new CartItem(
				entity.getId(),
				entity.getCartId(),
				entity.getProductId(),
				entity.getQuantity(),
				entity.getPrice(),
				entity.getDeliveryCost(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}

	public static CartItemEntity toEntity(final CartItem domain) {
		return new CartItemEntity(
				domain.getId(),
				domain.getCartId(),
				domain.getProductId(),
				domain.getQuantity(),
				domain.getPrice(),
				domain.getDeliveryCost(),
				domain.getCreatedAt(),
				domain.getUpdatedAt(),
				domain.getDeletedAt()
		);
	}

}
