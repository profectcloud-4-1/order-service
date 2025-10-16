package profect.group1.goormdotcom.cart.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

	private UUID id;
	private UUID cartId;
	private UUID productId;
	private int quantity;
	private int price;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public CartItem(
			final UUID cartId,
			final UUID productId,
			final int quantity,
			final int price
	) {
		this.cartId = cartId;
		this.productId = productId;
		this.quantity = quantity;
		this.price = price;
	}

	public void addQuantity(final int additionalQuantity) {
		this.quantity += additionalQuantity;
	}

	public void updateQuantity(final int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Quantity cannot be negative");
		}

		this.quantity = quantity;
	}
}
