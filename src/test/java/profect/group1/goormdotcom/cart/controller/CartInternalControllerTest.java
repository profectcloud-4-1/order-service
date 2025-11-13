package profect.group1.goormdotcom.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import profect.group1.goormdotcom.cart.controller.internal.v1.CartInternalController;
import profect.group1.goormdotcom.cart.service.CartService;
import profect.group1.goormdotcom.common.auth.LoginUserArgumentResolver;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CartInternalControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CartInternalController cartInternalController;

    @Mock
    private CartService cartService;

    @Mock
    private LoginUserArgumentResolver loginUserArgumentResolver;

    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartInternalController)
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("장바구니 생성 테스트")
    void createCart() throws Exception {
        // given
        UUID cartId = UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Id", userId.toString());
        given(cartService.createCart(userId)).willReturn(cartId);

        // when & then
        mockMvc.perform(post("/internal/v1/carts").headers(headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(cartId.toString()));
    }
}