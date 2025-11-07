package profect.group1.goormdotcom.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import profect.group1.goormdotcom.cart.controller.external.v1.CartController;
import profect.group1.goormdotcom.cart.controller.external.v1.dto.request.AddCartItemRequestDto;
import profect.group1.goormdotcom.cart.controller.external.v1.dto.request.DeleteBulkCartItemRequestDto;
import profect.group1.goormdotcom.cart.controller.external.v1.dto.request.UpdateCartItemRequestDto;
import profect.group1.goormdotcom.cart.domain.Cart;
import profect.group1.goormdotcom.cart.service.CartService;
import profect.group1.goormdotcom.common.auth.LoginUserArgumentResolver;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CartExternalControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    @Mock
    private LoginUserArgumentResolver loginUserArgumentResolver;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("장바구니 조회 테스트")
    void getCart() throws Exception {
        // given
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.getCart(userId)).willReturn(cart);

        // when & then
        mockMvc.perform(get("/api/v1/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 테스트")
    void addItemToCart() throws Exception {
        // given
        AddCartItemRequestDto requestDto = new AddCartItemRequestDto(UUID.randomUUID(), 1, 1000);
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.addCartItem(any(), any(), any(Integer.class), any(Integer.class))).willReturn(cart);

        // when & then
        mockMvc.perform(post("/api/v1/carts/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 변경 테스트")
    void updateItemToCart() throws Exception {
        // given
        UUID cartItemId = UUID.randomUUID();
        UpdateCartItemRequestDto requestDto = new UpdateCartItemRequestDto(5);
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.updateCartItem(any(), any(), any(Integer.class))).willReturn(cart);

        // when & then
        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("장바구니 아이템 대량 삭제 테스트")
    void deleteBulkItemFromCart() throws Exception {
        // given
        DeleteBulkCartItemRequestDto requestDto = new DeleteBulkCartItemRequestDto(List.of(UUID.randomUUID()));
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.removeBulkItem(any(), any())).willReturn(cart);

        // when & then
        mockMvc.perform(put("/api/v1/carts/items/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 테스트")
    void deleteItemFromCart() throws Exception {
        // given
        UUID cartItemId = UUID.randomUUID();
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.removeCartItem(any(), any())).willReturn(cart);

        // when & then
        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("장바구니 비우기 테스트")
    void deleteAllItemsFromCart() throws Exception {
        // given
        Cart cart = new Cart(UUID.randomUUID(), userId, Collections.emptyList(), null, null);
        given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(userId);
        given(cartService.clearCart(any())).willReturn(cart);

        // when & then
        mockMvc.perform(delete("/api/v1/carts/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }
}
