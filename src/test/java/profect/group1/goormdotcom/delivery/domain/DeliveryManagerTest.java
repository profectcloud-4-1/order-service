package profect.group1.goormdotcom.delivery.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStatus;
import profect.group1.goormdotcom.delivery.event.DeliveryStartFailedEvent;
import profect.group1.goormdotcom.delivery.infrastructure.client.DeliveryOrderClient;
import profect.group1.goormdotcom.delivery.repository.DeliveryAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnStepHistoryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryStepHistoryRepository;
import profect.group1.goormdotcom.delivery.repository.GoormAddressRepository;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryAddressEntity;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryEntity;
import profect.group1.goormdotcom.delivery.repository.entity.GoormAddressEntity;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryAddressMapper;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryReturnAddressMapper;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryReturnMapper;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryStepHistoryMapper;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryManagerTest {

    @InjectMocks
    private DeliveryManager deliveryManager;

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DeliveryReturnRepository deliveryReturnRepository;
    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;
    @Mock
    private GoormAddressRepository goormAddressRepository;
    @Mock
    private DeliveryReturnAddressRepository deliveryReturnAddressRepository;
    @Mock
    private DeliveryStepHistoryRepository deliveryStepHistoryRepository;
    @Mock
    private DeliveryReturnStepHistoryRepository deliveryReturnStepHistoryRepository;
    @Mock
    private DeliveryAddressMapper deliveryAddressMapper;
    @Mock
    private DeliveryReturnAddressMapper deliveryReturnAddressMapper;
    @Mock
    private DeliveryStepHistoryMapper deliveryStepHistoryMapper;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private DeliveryOrderClient orderClient;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID orderId;
    private DeliveryEntity deliveryEntity;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        deliveryEntity = DeliveryEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(DeliveryStatus.PENDING.getCode())
                .build();
    }

    @Test
    @DisplayName("startDelivery() - 배송 시작 테스트")
    void startDelivery() {
        // given
        when(goormAddressRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(GoormAddressEntity.builder().build()));
        when(deliveryRepository.save(any(DeliveryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Delivery delivery = deliveryManager.startDelivery(orderId, UUID.randomUUID(), "address", "detail", "zip", "phone", "name", "memo");

        // then
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("getDeliveryByOrderId() - 주문 ID로 배송 조회 테스트")
    void getDeliveryByOrderId() {
        // given
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(deliveryEntity));
        when(deliveryAddressRepository.findByDeliveryId(any())).thenReturn(Optional.of(DeliveryAddressEntity.builder().build()));
        when(deliveryStepHistoryRepository.findAllByDeliveryIdOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(deliveryAddressMapper.toDomainOfSender(any(DeliveryAddressEntity.class))).thenReturn(DeliveryAddress.builder().build());
        when(deliveryAddressMapper.toDomainOfReceiver(any(DeliveryAddressEntity.class))).thenReturn(DeliveryAddress.builder().build());

        // when
        Delivery delivery = deliveryManager.getDeliveryByOrderId(orderId);

        // then
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("cancel() - 배송 취소 테스트")
    void cancel() {
        // given
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(deliveryEntity));

        // when
        deliveryManager.cancel(orderId);

        // then
        verify(deliveryRepository).save(any(DeliveryEntity.class));
    }

    @Test
    @DisplayName("cancel() - 배송 취소 불가 테스트")
    void cancel_throwsException() {
        // given
        deliveryEntity.setStatus(DeliveryStatus.IN_DELIVERY.getCode());
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(deliveryEntity));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> deliveryManager.cancel(orderId));
    }

    @Test
    @DisplayName("returnDelivery() - 반품 테스트")
    void returnDelivery() {
        // given
        deliveryEntity.setStatus(DeliveryStatus.FINISH.getCode());
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(deliveryEntity));
        when(deliveryAddressRepository.findByDeliveryId(any())).thenReturn(Optional.of(DeliveryAddressEntity.builder().build()));
        when(deliveryReturnAddressMapper.toDomainOfSender(any())).thenReturn(DeliveryAddress.builder().build());
        when(deliveryReturnAddressMapper.toDomainOfReceiver(any())).thenReturn(DeliveryAddress.builder().build());

        // when
        DeliveryReturn deliveryReturn = deliveryManager.returnDelivery(orderId);

        // then
        assertThat(deliveryReturn).isNotNull();
    }

    @Test
    @DisplayName("getGoormAddress() - 구름 주소 조회 테스트")
    void getGoormAddress() {
        // given
        when(goormAddressRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(GoormAddressEntity.builder().build()));
        when(deliveryAddressMapper.toDomainFromGoormAddress(any(GoormAddressEntity.class))).thenReturn(DeliveryAddress.builder().build());

        // when
        DeliveryAddress deliveryAddress = deliveryManager.getGoormAddress();

        // then
        assertThat(deliveryAddress).isNotNull();
    }

    @Test
    @DisplayName("createGoormAddress() - 구름 주소 생성 테스트")
    void createGoormAddress() {
        // given
        when(goormAddressRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
        when(deliveryAddressMapper.toDomainFromGoormAddress(any(GoormAddressEntity.class))).thenReturn(DeliveryAddress.builder().build());

        // when
        DeliveryAddress deliveryAddress = deliveryManager.createGoormAddress("address", "detail", "zip", "phone", "name");

        // then
        assertThat(deliveryAddress).isNotNull();
    }

    @Test
    @DisplayName("updateGoormAddress() - 구름 주소 수정 테스트")
    void updateGoormAddress() {
        // given
        when(goormAddressRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(GoormAddressEntity.builder().build()));
        when(deliveryAddressMapper.toDomainFromGoormAddress(any(GoormAddressEntity.class))).thenReturn(DeliveryAddress.builder().build());

        // when
        DeliveryAddress deliveryAddress = deliveryManager.updateGoormAddress("address", "detail", "zip", "phone", "name");

        // then
        assertThat(deliveryAddress).isNotNull();
    }

    @Test
    @DisplayName("startDelivery() - Goorm 주소 없을 때 예외 발생 및 롤백 감지 테스트")
    void startDelivery_whenGoormAddressNotFound_throwsException() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        // Goorm 주소가 없어서 예외 발생하도록 설정
        when(goormAddressRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryManager.startDelivery(orderId, customerId, "address", "detail", "zip", "phone", "name", "memo");
        });
        
        // Note: 단위 테스트에서는 실제 트랜잭션이 없어서 TransactionSynchronization이 동작하지 않을 수 있습니다.
        // 통합 테스트를 통해 실제 롤백 감지를 검증해야 합니다.
    }
}
