package profect.group1.goormdotcom.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import profect.group1.goormdotcom.cart.domain.Cart;
import profect.group1.goormdotcom.cart.repository.CartItemRepository;
import profect.group1.goormdotcom.cart.repository.CartRepository;
import profect.group1.goormdotcom.cart.repository.entity.CartEntity;
import profect.group1.goormdotcom.cart.repository.entity.CartItemEntity;
import profect.group1.goormdotcom.cart.service.CartServiceImpl;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    private UUID customerId;
    private CartEntity cartEntity;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        cartEntity = new CartEntity(UUID.randomUUID(), customerId);
    }

    @Test
    @DisplayName("장바구니 생성 테스트")
    void createCart() {
        // given
        when(cartRepository.existsByCustomerId(customerId)).thenReturn(false);
        when(cartRepository.save(any(CartEntity.class))).thenReturn(cartEntity);

        // when
        UUID cartId = cartService.createCart(customerId);

        // then
        assertThat(cartId)
                .isEqualTo(cartEntity.getId());

        verify(cartRepository).existsByCustomerId(customerId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    @DisplayName("이미 장바구니가 존재할 경우 예외 발생")
    void createCart_alreadyExists() {
        // given
        when(cartRepository.existsByCustomerId(customerId)).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(customerId));
    }

    @Test
    @DisplayName("장바구니 조회 테스트")
    void getCart() {
        // given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartEntity.getId())).thenReturn(Collections.emptyList());

        // when
        Cart cart = cartService.getCart(customerId);

        // then
        assertThat(cart.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 테스트")
    void addCartItem() {
        // given
        UUID productId = UUID.randomUUID();
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartEntity.getId())).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Cart cart = cartService.addCartItem(customerId, productId, 1, 1000);

        // then
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("장바구니 아이템 수량 변경 테스트")
    void updateCartItem() {
        // given
        UUID productId = UUID.randomUUID();
        CartItemEntity cartItemEntity = new CartItemEntity(UUID.randomUUID(), cartEntity.getId(), productId, 1, 1000);
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartEntity.getId())).thenReturn(Collections.singletonList(cartItemEntity));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Cart cart = cartService.updateCartItem(customerId, cartItemEntity.getId(), 5);

        // then
        assertThat(cart.getCartItem(cartItemEntity.getId()).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 테스트")
    void removeCartItem() {
        // given
        UUID productId = UUID.randomUUID();
        CartItemEntity cartItemEntity = new CartItemEntity(UUID.randomUUID(), cartEntity.getId(), productId, 1, 1000);
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartEntity.getId())).thenReturn(Collections.singletonList(cartItemEntity));

        // when
        Cart cart = cartService.removeCartItem(customerId, cartItemEntity.getId());

        // then
        assertThat(cart.getItems()).isEmpty();
        verify(cartItemRepository).deleteById(cartItemEntity.getId());
    }

    @Test
    @DisplayName("장바구니 비우기 테스트")
    void clearCart() {
        // given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cartEntity));

        // when
        Cart cart = cartService.clearCart(customerId);

        // then
        assertThat(cart.getItems()).isEmpty();
        verify(cartItemRepository).deleteAllByCartId(cartEntity.getId());
    }
}
