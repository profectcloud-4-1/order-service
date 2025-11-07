package profect.group1.goormdotcom.order.controller;

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
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import profect.group1.goormdotcom.common.auth.LoginUser;
import profect.group1.goormdotcom.order.controller.external.v1.OrderController;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderItemDto;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderRequestDto;
import profect.group1.goormdotcom.order.domain.Order;
import profect.group1.goormdotcom.order.service.OrderService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
class OrderExternalControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private final UUID loginUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver loginUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(LoginUser.class)
                        && parameter.getParameterType().equals(UUID.class);
            }

            //테스트용 고정 ID 반환
            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                return loginUserId;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(loginUserResolver)
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void create_success() throws Exception {
        UUID productId = UUID.randomUUID();

        // item
        OrderItemDto item = new OrderItemDto();
        var f1 = OrderItemDto.class.getDeclaredField("productId");
        f1.setAccessible(true);
        f1.set(item, productId);
        var f2 = OrderItemDto.class.getDeclaredField("quantity");
        f2.setAccessible(true);
        f2.set(item, 2);

        // request
        OrderRequestDto req = new OrderRequestDto();
        for (var f : OrderRequestDto.class.getDeclaredFields()) {
            f.setAccessible(true);
            switch (f.getName()) {
                case "orderName" -> f.set(req, "테스트 주문");
                case "totalAmount" -> f.set(req, 10000);
                case "address" -> f.set(req, "주소");
                case "addressDetail" -> f.set(req, "상세주소");
                case "zipcode" -> f.set(req, "12345");
                case "phone" -> f.set(req, "010-1111-2222");
                case "name" -> f.set(req, "수령인");
                case "deliveryMemo" -> f.set(req, "문 앞");
                case "items" -> f.set(req, List.of(item));
            }
        }

        String body = new ObjectMapper().writeValueAsString(req);
        when(orderService.create(eq(loginUserId), any())).thenReturn(mock(Order.class));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(orderService).create(eq(loginUserId), any());
    }

    @Test
    @DisplayName("배송 전 취소 요청 시 200과 서비스 호출")
    void deliveryBefore_success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = mock(Order.class);

        when(orderService.delieveryBefore(orderId)).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel/delivery-before", orderId))
                .andExpect(status().isOk());

        verify(orderService).delieveryBefore(orderId);
    }

    @Test
    @DisplayName("배송 후 취소(반송) 요청 시 200과 서비스 호출")
    void cancel_success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = mock(Order.class);

        when(orderService.cancel(orderId)).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel/return", orderId))
                .andExpect(status().isOk());

        verify(orderService).cancel(orderId);
    }

    @Test
    @DisplayName("전체 주문 조회 시 200과 서비스 호출")
    void getAllOrders_success() throws Exception {
        when(orderService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());

        verify(orderService).getAll();
    }

    @Test
    @DisplayName("단건 주문 조회 시 200과 서비스 호출")
    void getOne_success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = mock(Order.class);

        when(orderService.getOne(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk());

        verify(orderService).getOne(orderId);
    }

    @Test
    @DisplayName("customerId, productId로 orderId 조회")
    void getOrderIdByCustomerAndProduct_success() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderService.getOrderIdByUserAndProduct(customerId, productId))
                .thenReturn(orderId);

        mockMvc.perform(
                        get("/api/v1/orders/search")
                                .param("customerId", customerId.toString())
                                .param("productId", productId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(orderId.toString())));

        verify(orderService).getOrderIdByUserAndProduct(customerId, productId);
    }
}