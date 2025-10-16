package profect.group1.goormdotcom.cart.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

	private UUID id;
	private UUID customerId;
	private int totalQuantity;
	private int totalPrice;
	private List<CartItem> items = new ArrayList<>();
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Cart(
			final UUID id,
			final UUID customerId,
			final List<CartItem> items,
			final LocalDateTime createdAt,
			final LocalDateTime updatedAt
	) {
		this.id = id;
		this.customerId = customerId;
		this.items = items;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;

		calculateTotal();
	}

	public void addItem(final CartItem item) {
		Optional<CartItem> existingItem = items.stream()
				.filter(i -> i.getProductId().equals(item.getProductId()))
				.findFirst();

		if (existingItem.isPresent()) {
			CartItem existingCartItem = existingItem.get();

			existingCartItem.addQuantity(item.getQuantity());
		} else {
			items.add(item);
		}

		calculateTotal();
	}

	public void updateItem(final UUID cartItemId, final int quantity) {
		Optional<CartItem> existingItem = items.stream()
				.filter(i -> i.getId().equals(cartItemId))
				.findFirst();

		if (existingItem.isEmpty()) {
			throw new IllegalArgumentException("Item not exists");
		}

		CartItem existingCartItem = existingItem.get();

		existingCartItem.updateQuantity(quantity);
		calculateTotal();
	}

	public boolean hasProduct(final UUID productId) {
		return items.stream()
				.anyMatch(i -> i.getProductId().equals(productId));
	}

	public CartItem getCartItem(final UUID cartItemId) {
		return items.stream().filter(e -> e.getId().equals(cartItemId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Item not exists"));
	}

	public CartItem getCartItemByProductId(final UUID productId) {
		return items.stream().filter(e -> e.getProductId().equals(productId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Item not exists"));
	}

	public void deleteItemById(final UUID itemId) {
		items.removeIf(i -> i.getId().equals(itemId));
		calculateTotal();
	}

	public void deleteBulkItem(final List<UUID> items) {
		items.forEach(this::deleteItemById);
		calculateTotal();
	}

	public void clear() {
		items.clear();
		calculateTotal();
	}

	public void calculateTotal() {
		this.totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
		this.totalPrice = items.stream()
				.mapToInt(item -> item.getPrice() * item.getQuantity())
				.sum();
	}
}
