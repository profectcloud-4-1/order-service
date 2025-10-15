package profect.group1.goormdotcom.cart.controller.v1;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;
import profect.group1.goormdotcom.cart.controller.dto.CartResponseDto;
import profect.group1.goormdotcom.cart.controller.mapper.CartDtoMapper;
import profect.group1.goormdotcom.cart.domain.Cart;
import profect.group1.goormdotcom.cart.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CartController {

	private final CartService cartService;

	@GetMapping
	public ApiResponse<CartResponseDto> getCart() {
		Cart cart = cartService.getCart(null);

		return ApiResponse.of(SuccessStatus._OK, CartDtoMapper.toDto(cart));
	}

}
