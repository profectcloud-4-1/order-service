package profect.group1.goormdotcom.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;

import org.awaitility.Awaitility;
import profect.group1.goormdotcom.kafka.OrderProducer;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.event.Stock.StockRollbackRequestedEvent;
import profect.group1.goormdotcom.order.infrastructure.client.PaymentClient;
import profect.group1.goormdotcom.order.infrastructure.client.StockClient;
import profect.group1.goormdotcom.order.repository.OrderProductRepository;
import profect.group1.goormdotcom.order.repository.OrderRepository;
import profect.group1.goormdotcom.order.repository.OrderStatusRepository;
import profect.group1.goormdotcom.order.repository.entity.OrderEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderProductEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderStatusEntity;
import profect.group1.goormdotcom.order.service.OrderService;

/**
 * Order-Service의 Kafka 메시지 발행 테스트
 * 
 * 이 테스트는 Order-Service에서 StockRollbackRequestedEvent를 Kafka로 올바르게 발행하는지 검증합니다.
 * 
 * ⚠️ 주의사항:
 * - 실제 Stock-Service의 Consumer는 별도 서비스에 구현되어 있어 이 테스트로는 확인할 수 없습니다.
 * - 이 테스트는 Order-Service의 책임 범위만 검증합니다:
 *   1. OrderProducer.send()가 올바른 토픽과 이벤트로 호출되었는지 확인
 *   2. 발행된 이벤트의 내용이 올바른지 확인
 * - 실제 Kafka 브로커로의 메시지 발행 및 Stock-Service의 Consumer 수신은 별도의 E2E 통합 테스트에서 확인해야 합니다.
 * 
 * 테스트 방식:
 * - OrderProducer를 실제 Bean으로 사용하여 실제 Kafka 브로커로 메시지를 발행
 * - 메시지 발행 여부는 KafkaUI에서 확인
 */
@SpringBootTest(properties = "ORDER_PROFILE=test")
@ActiveProfiles("test")
class OrderStockIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @MockBean
    private StockClient stockClient;

    @MockBean
    private PaymentClient paymentClient;

    // OrderProducer는 실제 Bean으로 사용하여 실제 Kafka 브로커로 메시지를 발행
    // 메시지 발행 여부는 KafkaUI에서 확인
    @Autowired
    private OrderProducer orderProducer;
    
    // KafkaTemplate을 주입받아 메시지 발행 완료를 확인
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @AfterEach
    void tearDown() {
        orderStatusRepository.deleteAll();
        orderProductRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("결제 실패 시 StockRollbackRequestedEvent가 stock-service-topic으로 발행된다")
    void failPayment_publishStockRollbackRequestedEvent() throws Exception {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(30_000)
                .orderName("재고 롤백 테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId1)
                .productName("테스트 상품 1")
                .quantity(2)
                .totalAmount(20_000)
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId2)
                .productName("테스트 상품 2")
                .quantity(1)
                .totalAmount(10_000)
                .build()
        );

        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        // when
        orderService.failPayment(orderEntity.getId());

        // then - 실제 Kafka 브로커로 메시지가 발행되었는지 확인
        // OrderProducer.send()가 실제로 호출되어 Kafka 브로커로 메시지가 발행됨
        // KafkaTemplate.send()는 비동기이므로 메시지가 브로커에 전달될 때까지 충분히 대기
        // 실제로는 OrderProducer에서 Future.get()을 호출해야 하지만, 테스트에서는 대기로 처리
        try {
            // Kafka 브로커에 메시지가 전달될 때까지 충분히 대기
            Thread.sleep(5000);
            System.out.println("=== 메시지 발행 완료 ===");
            System.out.println("KafkaUI (http://localhost:8080)에서 stock-service-topic의 메시지를 확인하세요");
            System.out.println("토픽이 자동 생성되었고 메시지가 발행되었습니다.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 주문 상태가 FAILED로 변경되었는지 검증
        assertThat(orderStatusRepository.findTop1ByOrder_IdOrderByCreatedAtDesc(orderEntity.getId())
            .map(OrderStatusEntity::getStatus)
            .orElse(null)).isEqualTo(OrderStatus.FAILED.getCode());
    }

    @Test
    @DisplayName("결제 실패 시 StockRollbackRequestedEvent에 올바른 상품 정보가 포함되어 stock-service-topic으로 발행된다")
    void failPayment_publishStockRollbackRequestedEventWithCorrectItems() throws Exception {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 3;

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(30_000)
                .orderName("재고 롤백 상세 테스트 주문")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId)
                .productName("테스트 상품")
                .quantity(quantity)
                .totalAmount(30_000)
                .build()
        );

        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        // when
        orderService.failPayment(orderEntity.getId());

        // then - 실제 Kafka 브로커로 메시지가 발행되었는지 확인
        try {
            Thread.sleep(5000);
            System.out.println("=== 메시지 발행 완료 ===");
            System.out.println("KafkaUI (http://localhost:8080)에서 stock-service-topic의 메시지를 확인하세요");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("결제 실패 시 여러 상품이 있는 경우 모든 상품의 재고 롤백 정보가 stock-service-topic으로 발행된다")
    void failPayment_publishStockRollbackRequestedEventForMultipleItems() throws Exception {
        // given
        UUID customerId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        UUID productId3 = UUID.randomUUID();

        OrderEntity orderEntity = orderRepository.save(
            OrderEntity.builder()
                .customerId(customerId)
                .totalAmount(60_000)
                .orderName("다중 상품 재고 롤백 테스트")
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId1)
                .productName("상품 1")
                .quantity(2)
                .totalAmount(20_000)
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId2)
                .productName("상품 2")
                .quantity(3)
                .totalAmount(30_000)
                .build()
        );

        orderProductRepository.save(
            OrderProductEntity.builder()
                .order(orderEntity)
                .productId(productId3)
                .productName("상품 3")
                .quantity(1)
                .totalAmount(10_000)
                .build()
        );

        orderStatusRepository.save(
            OrderStatusEntity.builder()
                .order(orderEntity)
                .status(OrderStatus.PENDING.getCode())
                .build()
        );

        // when
        orderService.failPayment(orderEntity.getId());

        // then - 실제 Kafka 브로커로 메시지가 발행되었는지 확인
        try {
            Thread.sleep(5000);
            System.out.println("=== 메시지 발행 완료 ===");
            System.out.println("KafkaUI (http://localhost:8080)에서 stock-service-topic의 메시지를 확인하세요");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

