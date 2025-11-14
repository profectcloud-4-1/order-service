package profect.group1.goormdotcom.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.timeout;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.ActiveProfiles;

import org.awaitility.Awaitility;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.service.DeliveryService;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.infrastructure.client.DeliveryClient;
import profect.group1.goormdotcom.order.infrastructure.client.PaymentClient;
import profect.group1.goormdotcom.order.infrastructure.client.StockClient;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentRequestDto;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentResponseDto;
import profect.group1.goormdotcom.order.repository.OrderAddressRepository;
import profect.group1.goormdotcom.order.repository.OrderProductRepository;
import profect.group1.goormdotcom.order.repository.OrderRepository;
import profect.group1.goormdotcom.order.repository.OrderStatusRepository;
import profect.group1.goormdotcom.order.repository.entity.OrderAddressEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderProductEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderStatusEntity;
import profect.group1.goormdotcom.order.service.OrderService;

@SpringBootTest(properties = "ORDER_PROFILE=test")
@ActiveProfiles("test")
class OrderDeliveryIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderAddressRepository orderAddressRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean
    private StockClient stockClient;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private DeliveryClient deliveryClient;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        orderStatusRepository.deleteAll();
        orderProductRepository.deleteAll();
        orderAddressRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("결제 성공 이벤트가 배송 서비스까지 전달된다")
    void completePayment_publishDeliveryRequestedEvent() throws InterruptedException {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(20_000)
                .orderName("통합 테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderAddressRepository.save(
            OrderAddressEntity.builder()
                .orderId(orderEntity.getId())
                .customerId(customerId)
                .address("서울시 테스트로 123")
                .addressDetail("테스트 아파트 101동 1001호")
                .zipcode("01234")
                .phone("010-1234-5678")
                .name("홍길동")
                .deliveryMemo("문 앞에 두세요")
                .build()
        );

        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        when(deliveryService.startDelivery(
            any(UUID.class),
            any(UUID.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class)
        )).thenReturn(Delivery.builder().build());

        // when
        orderService.completePayment(orderEntity.getId());

        // then
        verify(deliveryService, timeout(5000).times(1)).startDelivery(
            eq(orderEntity.getId()),
            eq(customerId),
            eq("서울시 테스트로 123"),
            eq("테스트 아파트 101동 1001호"),
            eq("01234"),
            eq("010-1234-5678"),
            eq("홍길동"),
            eq("문 앞에 두세요")
        );

        Awaitility.await("주문 상태가 COMPLETED로 갱신될 때까지 대기")
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .until(() -> orderStatusRepository.findTop1ByOrder_IdOrderByCreatedAtDesc(orderEntity.getId())
                .map(OrderStatusEntity::getStatus)
                .orElse(null), OrderStatus.COMPLETED.getCode()::equals);
    }

    @Test
    @DisplayName("결제 실패 이벤트가 배송 취소까지 전달된다")
    void failPayment_publishDeliveryCancellationEvent() {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(20_000)
                .orderName("통합 테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId)
                .productName("테스트 상품")
                .quantity(1)
                .totalAmount(20_000)
                .build()
        );
//      재고 복구 테스트
        StockAdjustmentResponseDto stockResponse = new StockAdjustmentResponseDto(true, List.of());
        when(stockClient.increaseStock(any(StockAdjustmentRequestDto.class)))
            .thenReturn(ApiResponse.onSuccess(stockResponse));
        when(deliveryService.cancel(orderEntity.getId())).thenReturn(true);

        // when
        orderService.failPayment(orderEntity.getId());

        // then
        verify(deliveryService, timeout(5000).times(1)).cancel(orderEntity.getId());
    }

    @Test
    @DisplayName("결제 처리 중 롤백되면 배송 취소 보상 이벤트가 발행된다")
    void rollbackDuringCompletePayment_publishesCompensationEvent() {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(20_000)
                .orderName("롤백 테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderAddressRepository.save(
            OrderAddressEntity.builder()
                .orderId(orderEntity.getId())
                .customerId(customerId)
                .address("서울시 테스트로 456")
                .addressDetail("테스트 아파트 202동 2002호")
                .zipcode("01235")
                .phone("010-9876-5432")
                .name("김테스트")
                .deliveryMemo("경비실에 맡겨주세요")
                .build()
        );

        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        TransactionTemplate template = new TransactionTemplate(transactionManager);

        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            template.execute(status -> {
                orderService.completePayment(orderEntity.getId()); //결제 완료 처리
                throw new RuntimeException("강제 롤백"); //강제 롤백
            })
        ).isInstanceOf(RuntimeException.class);

        // then
        verify(deliveryService, timeout(5000).times(1)).cancel(orderEntity.getId());
    }
}

