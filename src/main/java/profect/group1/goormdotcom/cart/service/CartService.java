package profect.group1.goormdotcom.cart.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.group1.goormdotcom.cart.domain.Cart;
import profect.group1.goormdotcom.cart.domain.CartItem;
import profect.group1.goormdotcom.cart.repository.CartItemRepository;
import profect.group1.goormdotcom.cart.repository.CartRepository;
import profect.group1.goormdotcom.cart.repository.entity.CartEntity;
import profect.group1.goormdotcom.cart.repository.mapper.CartItemMapper;
import profect.group1.goormdotcom.cart.repository.mapper.CartMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	public Cart getCart(final UUID customerId) {
		CartEntity entity = cartRepository.getCartEntityByCustomerId(customerId);
		List<CartItem> items = cartItemRepository.findByCartId(entity.getId()).stream()
				.map(CartItemMapper::toDomain)
				.toList();

		return CartMapper.toDomain(entity, items);
	}

	public void addCart() {
	}

	public void updateCartItem() {
	}

	public void deleteCartItem() {
	}

	public void deleteBulkItem() {
	}

	public void clearCart() {
	}

}
