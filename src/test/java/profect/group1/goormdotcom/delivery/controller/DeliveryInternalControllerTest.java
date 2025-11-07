package profect.group1.goormdotcom.delivery.controller;

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
import profect.group1.goormdotcom.delivery.controller.internal.v1.DeliveryInternalController;
import profect.group1.goormdotcom.delivery.controller.internal.v1.request.CancelDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.controller.internal.v1.request.StartDeliveryRequestDto;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.service.DeliveryService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DeliveryInternalControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private DeliveryInternalController deliveryInternalController;

    @Mock
    private DeliveryService deliveryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UUID orderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deliveryInternalController).build();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("반품 가능 여부 확인 테스트")
    void checkCancellable() throws Exception {
        // given
        given(deliveryService.canReturn(any(UUID.class))).willReturn(1);

        // when & then
        mockMvc.perform(get("/internal/v1/delivery/check/cancellable").param("orderId", orderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    @DisplayName("배송 시작 테스트")
    void startDelivery() throws Exception {
        // given
        StartDeliveryRequestDto requestDto = new StartDeliveryRequestDto(orderId, UUID.randomUUID(), "address", "detail", "zip", "phone", "name", "memo");
        Delivery delivery = Delivery.builder().id(UUID.randomUUID()).build();
        given(deliveryService.startDelivery(any(), any(), any(), any(), any(), any(), any(), any())).willReturn(delivery);

        // when & then
        mockMvc.perform(post("/internal/v1/delivery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("배송 취소 테스트")
    void cancelDelivery() throws Exception {
        // given
        CancelDeliveryRequestDto requestDto = new CancelDeliveryRequestDto(orderId);
        given(deliveryService.cancel(any(UUID.class))).willReturn(true);

        // when & then
        mockMvc.perform(post("/internal/v1/delivery/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("반품 테스트")
    void returnDelivery() throws Exception {
        // given
        CancelDeliveryRequestDto requestDto = new CancelDeliveryRequestDto(orderId);
        given(deliveryService.returnDelivery(any(UUID.class))).willReturn(null);

        // when & then
        mockMvc.perform(post("/internal/v1/delivery/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
