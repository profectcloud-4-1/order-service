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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import profect.group1.goormdotcom.delivery.controller.external.v1.DeliveryController;
import profect.group1.goormdotcom.delivery.controller.external.v1.dto.request.CreateAddressRequestDto;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.service.DeliveryService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DeliveryExternalControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private DeliveryController deliveryController;

    @Mock
    private DeliveryService deliveryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UUID orderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deliveryController).build();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("주문 ID로 배송 조회 테스트")
    @WithMockUser
    void getDeliveryByOrder() throws Exception {
        // given
        Delivery delivery = Delivery.builder().id(UUID.randomUUID()).orderId(orderId).build();
        given(deliveryService.getDeliveryByOrderId(any(UUID.class))).willReturn(delivery);

        // when & then
        mockMvc.perform(get("/api/v1/delivery/by-order").param("orderId", orderId.toString()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.delivery").exists())
                .andExpect(jsonPath("$.result.delivery.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("구름 주소 조회 테스트")
    @WithMockUser(roles = "MASTER")
    void getGoormAddress() throws Exception {
        // given
        DeliveryAddress address = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        given(deliveryService.getGoormAddress()).willReturn(address);

        // when & then
        mockMvc.perform(get("/api/v1/delivery/address/goorm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("구름 주소 생성 테스트")
    @WithMockUser(roles = "MASTER")
    void createGoormAddress() throws Exception {
        // given
        CreateAddressRequestDto requestDto = new CreateAddressRequestDto(UUID.randomUUID(), "address", "detail", "zip", "phone", "name");
        DeliveryAddress address = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        given(deliveryService.createGoormAddress(any(CreateAddressRequestDto.class))).willReturn(address);

        // when & then
        mockMvc.perform(post("/api/v1/delivery/address/goorm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("구름 주소 수정 테스트")
    @WithMockUser(roles = "MASTER")
    void updateGoormAddress() throws Exception {
        // given
        CreateAddressRequestDto requestDto = new CreateAddressRequestDto(UUID.randomUUID(), "address", "detail", "zip", "phone", "name");
        DeliveryAddress address = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        given(deliveryService.updateGoormAddress(any(CreateAddressRequestDto.class))).willReturn(address);

        // when & then
        mockMvc.perform(put("/api/v1/delivery/address/goorm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }
}
