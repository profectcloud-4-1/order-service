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
	private int deliveryCost;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
}
