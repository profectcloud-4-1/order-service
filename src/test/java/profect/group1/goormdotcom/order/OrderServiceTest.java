package profect.group1.goormdotcom.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderItemDto;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderRequestDto;
import profect.group1.goormdotcom.order.domain.Order;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.domain.mapper.OrderMapper;
import profect.group1.goormdotcom.order.event.DeliveryEventPublisherInterface;
import profect.group1.goormdotcom.order.event.DeliveryCancellationRequestedEvent;
import profect.group1.goormdotcom.order.event.DeliveryRequestedEvent;
import profect.group1.goormdotcom.order.infrastructure.client.DeliveryClient;
import profect.group1.goormdotcom.order.infrastructure.client.PaymentClient;
import profect.group1.goormdotcom.order.infrastructure.client.StockClient;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentRequestDto;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentResponseDto;
import profect.group1.goormdotcom.order.repository.*;
import profect.group1.goormdotcom.order.repository.entity.*;
import profect.group1.goormdotcom.order.service.OrderService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private OrderRepository orderRepository;
    @Mock private OrderProductRepository orderProductRepository;
    @Mock private OrderStatusRepository orderStatusRepository;
    @Mock private OrderAddressRepository orderAddressRepository;
    @Mock private StockClient stockClient;
    @SuppressWarnings("unused")
    @Mock private PaymentClient paymentClient;
    @SuppressWarnings("unused")
    @Mock private DeliveryClient deliveryClient;
    @Mock private OrderMapper orderMapper;
    @Mock private DeliveryEventPublisherInterface deliveryEventPublisher;

    @Mock
    private OrderRequestDto orderRequestDto;

    private UUID userId;
    private UUID orderId;
    private UUID productId;

    private OrderStatusEntity orderStatusEntity;
    private OrderProductEntity orderProductEntity;
    private OrderAddressEntity orderAddressEntity;
    private OrderEntity savedOrderEntity;
    private Order orderDomain;

    private void initTestData() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();

        savedOrderEntity = OrderEntity.builder()
                .id(orderId)
                .customerId(userId)
                .totalAmount(10000)
                .orderName("테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build();

        orderDomain = Order.builder()
                .id(orderId)
                .customerId(userId)
                .totalAmount(10000)
                .orderName("테스트 주문")
                .status(OrderStatus.PENDING)
                .build();

        orderAddressEntity = OrderAddressEntity.builder()
                .orderId(orderId)
                .customerId(userId)
                .address("주소")
                .addressDetail("상세주소")
                .zipcode("12345")
                .phone("010-1111-2222")
                .name("수령인")
                .deliveryMemo("문 앞")
                .build();

        orderProductEntity = OrderProductEntity.builder()
                .id(UUID.randomUUID())
                .order(savedOrderEntity)
                .productId(productId)
                .productName("상품명")
                .quantity(2)
                .totalAmount(10000)
                .build();

        orderStatusEntity = OrderStatusEntity.builder()
                .id(UUID.randomUUID())
                .order(savedOrderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build();
    }

    @Test
    @DisplayName("create() - 재고 차감 성공 시 주문이 생성되고 매퍼 도메인 객체를 반환한다")
    void create_success() {
        // given
        initTestData();
        OrderItemDto item = mock(OrderItemDto.class);
        when(item.getProductId()).thenReturn(productId);
        when(item.getQuantity()).thenReturn(2);
        when(orderRequestDto.getItems()).thenReturn(List.of(item));
        when(orderRequestDto.getTotalAmount()).thenReturn(10000);
        when(orderRequestDto.getOrderName()).thenReturn("테스트 주문");
        when(orderRequestDto.getAddress()).thenReturn("주소");
        when(orderRequestDto.getAddressDetail()).thenReturn("상세주소");
        when(orderRequestDto.getZipcode()).thenReturn("12345");
        when(orderRequestDto.getPhone()).thenReturn("010-1111-2222");
        when(orderRequestDto.getName()).thenReturn("수령인");
        when(orderRequestDto.getDeliveryMemo()).thenReturn("문 앞");

        StockAdjustmentResponseDto stockResult = mock(StockAdjustmentResponseDto.class);
        when(stockResult.status()).thenReturn(true);
        when(stockClient.decreaseStocks(any()))
                .thenReturn(ApiResponse.onSuccess(stockResult));

        when(orderRepository.save(any())).thenReturn(savedOrderEntity);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrderEntity));
        when(orderMapper.toDomain(any())).thenReturn(orderDomain);

        // when
        Order result = orderService.create(userId, orderRequestDto);

        // then
        assertThat(result.getId()).isEqualTo(orderId);

        verify(stockClient).decreaseStocks(any());
        verify(orderRepository).save(any());
        verify(orderMapper).toDomain(any());
        verifyNoInteractions(paymentClient, deliveryClient);
    }

    @Test
    @DisplayName("create() - 재고 차감 실패 시 예외를 던진다")
    void create_stockFail_throwsException() throws NoSuchFieldException, IllegalAccessException {
        // given:
        initTestData();
        OrderItemDto orderItemDto = new OrderItemDto();
        Field productIdField = OrderItemDto.class.getDeclaredField("productId");
        productIdField.setAccessible(true);
        productIdField.set(orderItemDto, productId);
        Field quantityField = OrderItemDto.class.getDeclaredField("quantity");
        quantityField.setAccessible(true);
        quantityField.set(orderItemDto, 2);

        List<OrderItemDto> items = List.of(orderItemDto);
        when(orderRequestDto.getItems()).thenReturn(items);

        StockAdjustmentResponseDto stockResult = mock(StockAdjustmentResponseDto.class);
        when(stockResult.status()).thenReturn(false); // 실패
        when(stockClient.decreaseStocks(any(StockAdjustmentRequestDto.class)))
                .thenReturn(ApiResponse.onSuccess(stockResult));

        // when & then
        assertThatThrownBy(() -> orderService.create(userId, orderRequestDto))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any());
        verify(orderProductRepository, never()).saveAll(anyList());
        verify(orderAddressRepository, never()).save(any());
        verify(orderStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("completePayment() - 결제 완료 시 상태를 PAID로 기록하고 도메인 객체를 반환한다")
    void completePayment_success() {
        // given
        initTestData();
        OrderEntity orderEntity = savedOrderEntity;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderAddressRepository.findByOrderId(orderId)).thenReturn(Optional.of(orderAddressEntity));

        when(orderStatusRepository.save(any(OrderStatusEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toDomain(orderEntity)).thenReturn(orderDomain);

        // when
        Order result = orderService.completePayment(orderId);

        // then
        assertThat(result.getId()).isEqualTo(orderId);

        verify(deliveryEventPublisher).publishDeliveryRequested(any(DeliveryRequestedEvent.class));
        ArgumentCaptor<OrderStatusEntity> statusCaptor = ArgumentCaptor.forClass(OrderStatusEntity.class);
        verify(orderStatusRepository, atLeastOnce()).save(statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PAID.getCode());
        verify(orderMapper, times(1)).toDomain(orderEntity);
        verifyNoInteractions(paymentClient, deliveryClient);
    }

    @Test
    @DisplayName("failPayment() - 재고 복구 성공 시 상태를 FAILED로 기록한다")
    void failPayment_success() {
        // given
        initTestData();
        OrderEntity orderEntity = savedOrderEntity;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderProductRepository.findByOrderId(orderId)).thenReturn(List.of(orderProductEntity));

        StockAdjustmentResponseDto stockResult = mock(StockAdjustmentResponseDto.class);
        when(stockResult.status()).thenReturn(true);

        when(stockClient.increaseStock(any(StockAdjustmentRequestDto.class)))
                .thenReturn(ApiResponse.onSuccess(stockResult));

        when(orderStatusRepository.save(any(OrderStatusEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toDomain(orderEntity)).thenReturn(orderDomain);

        // when
        Order result = orderService.failPayment(orderId);

        // then
        assertThat(result.getId()).isEqualTo(orderId);

        verify(stockClient, times(1)).increaseStock(any(StockAdjustmentRequestDto.class));
        verify(orderStatusRepository, times(1)).save(any(OrderStatusEntity.class));
        verify(deliveryEventPublisher).publishDeliveryCancellationRequested(any(DeliveryCancellationRequestedEvent.class));
        verifyNoInteractions(paymentClient, deliveryClient);
    }

    @Test
    @DisplayName("getOne() - 주문 ID로 조회 시 도메인 객체를 반환한다")
    void getOne_success() {
        // given
        initTestData();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrderEntity));

        when(orderStatusRepository.findTop1ByOrder_IdOrderByCreatedAtDesc(orderId))
                .thenReturn(Optional.of(orderStatusEntity));

        when(orderMapper.toDomain(savedOrderEntity)).thenReturn(orderDomain);

        // when
        Order result = orderService.getOne(orderId);

        // then
        assertThat(result.getId()).isEqualTo(orderId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderStatusRepository, times(1))
                .findTop1ByOrder_IdOrderByCreatedAtDesc(orderId);
        verify(orderMapper, times(1)).toDomain(savedOrderEntity);
    }

    @Test
    @DisplayName("getOrderIdByUserAndProduct() - userId와 productId로 주문을 찾아 ID를 반환한다")
    void getOrderIdByUserAndProduct_success() {
        // given
        initTestData();
        OrderEntity order = savedOrderEntity;
        when(orderRepository.findByCustomerIdAndProductId(userId, productId))
                .thenReturn(Optional.of(order));

        // when
        UUID result = orderService.getOrderIdByUserAndProduct(userId, productId);

        // then
        assertThat(result).isEqualTo(orderId);

        verify(orderRepository, times(1))
                .findByCustomerIdAndProductId(userId, productId);
    }
}