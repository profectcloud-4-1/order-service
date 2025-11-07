package profect.group1.goormdotcom.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.repository.OrderAddressRepository;
import profect.group1.goormdotcom.order.repository.OrderProductRepository;
import profect.group1.goormdotcom.order.repository.OrderRepository;
import profect.group1.goormdotcom.order.repository.OrderStatusRepository;
import profect.group1.goormdotcom.order.repository.entity.OrderAddressEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderProductEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderStatusEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderAddressRepository orderAddressRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("OrderRepository - 고객 ID와 상품 ID로 주문 찾기 테스트")
    void findByCustomerIdAndProductId() {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(customerId)
                .build();
        entityManager.persist(orderEntity);

        OrderProductEntity orderProductEntity = OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId)
                .productName("test product")
                .quantity(1)
                .totalAmount(1000)
                .build();
        entityManager.persist(orderProductEntity);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<OrderEntity> foundOrder =
                orderRepository.findByCustomerIdAndProductId(customerId, productId);

        // then - 한 번의 assertThat으로 핵심만
        assertThat(foundOrder)
                .get()
                .extracting(OrderEntity::getCustomerId)
                .isEqualTo(customerId);
    }

    @Test
    @DisplayName("OrderAddressRepository - 주문 ID로 주소 찾기 테스트")
    void findByOrderId_OrderAddress() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderAddressEntity addressEntity = OrderAddressEntity.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .address("address")
                .addressDetail("address detail")
                .zipcode("12345")
                .phone("010-1234-5678")
                .name("test user")
                .deliveryMemo("memo")
                .build();
        entityManager.persist(addressEntity);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<OrderAddressEntity> foundAddress =
                orderAddressRepository.findByOrderId(orderId);

        // then
        assertThat(foundAddress)
                .get()
                .extracting(OrderAddressEntity::getOrderId)
                .isEqualTo(orderId);
    }

    @Test
    @DisplayName("OrderProductRepository - 주문 ID로 상품 목록 찾기 테스트")
    void findByOrderId_OrderProduct() {
        // given
        OrderEntity orderEntity = OrderEntity.builder().build();
        entityManager.persist(orderEntity);

        OrderProductEntity product1 = OrderProductEntity.builder()
                .order(orderEntity)
                .productId(UUID.randomUUID())
                .productName("p1")
                .quantity(1)
                .totalAmount(100)
                .build();

        OrderProductEntity product2 = OrderProductEntity.builder()
                .order(orderEntity)
                .productId(UUID.randomUUID())
                .productName("p2")
                .quantity(1)
                .totalAmount(100)
                .build();

        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<OrderProductEntity> foundProducts =
                orderProductRepository.findByOrderId(orderEntity.getId());

        // then (핵심: 갯수)
        assertThat(foundProducts).hasSize(2);
    }

    @Test
    @DisplayName("OrderStatusRepository - 주문 ID로 최신 상태 찾기 테스트")
    void findTop1ByOrder_IdOrderByCreatedAtDesc() throws Exception {
        // given
        OrderEntity orderEntity = OrderEntity.builder().build();
        entityManager.persist(orderEntity);

        OrderStatusEntity status1 = OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build();

        OrderStatusEntity status2 = OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.COMPLETED.getCode())
                .build();

        // createdAt 리플렉션
        Field createdAtField = status1.getClass().getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(status1, java.time.LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        createdAtField.set(status2, java.time.LocalDateTime.of(2024, 1, 1, 0, 0, 1));

        entityManager.persist(status1);
        entityManager.persist(status2);
        entityManager.flush();
        entityManager.clear();

        // when
        var foundStatus = orderStatusRepository.findTop1ByOrder_IdOrderByCreatedAtDesc(orderEntity.getId());

        // then
        assertThat(foundStatus)
                .get()
                .extracting(OrderStatusEntity::getStatus)
                .isEqualTo(OrderStatus.COMPLETED.getCode());
    }
}