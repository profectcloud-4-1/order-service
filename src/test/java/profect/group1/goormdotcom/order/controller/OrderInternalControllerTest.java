package profect.group1.goormdotcom.order.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import profect.group1.goormdotcom.order.controller.internal.v1.OrderInternalController;
import profect.group1.goormdotcom.order.domain.Order;
import profect.group1.goormdotcom.order.service.OrderService;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderInternalControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderInternalController orderInternalController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderInternalController)
                .build();
    }

    @Test
    @DisplayName("결제 성공 콜백 시 서비스 completePayment 호출")
    void completePayment_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderService.completePayment(orderId)).thenReturn(mock(Order.class));

        // when
        mockMvc.perform(post("/internal/v1/orders/{orderId}/payment/success", orderId))
                // then
                .andExpect(status().isOk());

        verify(orderService).completePayment(orderId);
    }

    @Test
    @DisplayName("결제 실패 콜백 시 서비스 failPayment 호출")
    void failPayment_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderService.failPayment(orderId)).thenReturn(mock(Order.class));

        // when
        mockMvc.perform(post("/internal/v1/orders/{orderId}/payment/fail", orderId))
                // then
                .andExpect(status().isOk());

        verify(orderService).failPayment(orderId);
    }

    @Test
    @DisplayName("반송 완료 콜백 시 서비스 deliveryReturnCompleted 호출")
    void deliveryReturnCompleted_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderService).deliveryReturnCompleted(orderId);

        // when
        mockMvc.perform(post("/internal/v1/orders/api/v1/orders/{orderId}/return-completed", orderId))
                // then
                .andExpect(status().isOk());

        verify(orderService).deliveryReturnCompleted(orderId);
    }
}