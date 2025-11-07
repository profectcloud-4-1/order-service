package profect.group1.goormdotcom.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import profect.group1.goormdotcom.delivery.controller.external.v1.dto.request.CreateAddressRequestDto;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.domain.DeliveryManager;
import profect.group1.goormdotcom.delivery.domain.DeliveryReturn;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStatus;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryEntity;
import profect.group1.goormdotcom.delivery.service.DeliveryServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    @Mock
    private DeliveryManager deliveryManager;

    @Mock
    private DeliveryRepository deliveryRepository;

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
    @DisplayName("배송 가능 여부 확인: 배송 시작 전이면 1을 반환한다")
    void canReturn_beforeDelivery_returns1() {
        // given
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(deliveryEntity));

        // when
        Integer result = deliveryService.canReturn(orderId);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("배송 시작을 위임하고 결과를 반환한다")
    void startDelivery_delegatesToManager() {
        // given
        Delivery delivery = Delivery.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.startDelivery(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(delivery);

        // when
        Delivery result = deliveryService.startDelivery(orderId, UUID.randomUUID(), "address", "detail", "zip", "phone", "name", "memo");

        // then
        assertThat(result).isEqualTo(delivery);
    }

    @Test
    @DisplayName("배송 취소를 위임한다")
    void cancel_delegatesToManager() {
        // when
        deliveryService.cancel(orderId);

        // then
        verify(deliveryManager).cancel(orderId);
    }

    @Test
    @DisplayName("반품을 위임하고 결과를 반환한다")
    void returnDelivery_delegatesToManager() {
        // given
        DeliveryReturn deliveryReturn = DeliveryReturn.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.returnDelivery(orderId)).thenReturn(deliveryReturn);

        // when
        DeliveryReturn result = deliveryService.returnDelivery(orderId);

        // then
        assertThat(result).isEqualTo(deliveryReturn);
    }

    @Test
    @DisplayName("구름 주소 조회를 위임하고 결과를 반환한다")
    void getGoormAddress_delegatesToManager() {
        // given
        DeliveryAddress deliveryAddress = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.getGoormAddress()).thenReturn(deliveryAddress);

        // when
        DeliveryAddress result = deliveryService.getGoormAddress();

        // then
        assertThat(result).isEqualTo(deliveryAddress);
    }

    @Test
    @DisplayName("구름 주소 생성을 위임하고 결과를 반환한다")
    void createGoormAddress_delegatesToManager() {
        // given
        CreateAddressRequestDto requestDto = new CreateAddressRequestDto(UUID.randomUUID(), "address", "detail", "zip", "phone", "name");
        DeliveryAddress deliveryAddress = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.createGoormAddress(any(), any(), any(), any(), any())).thenReturn(deliveryAddress);

        // when
        DeliveryAddress result = deliveryService.createGoormAddress(requestDto);

        // then
        assertThat(result).isEqualTo(deliveryAddress);
    }

    @Test
    @DisplayName("구름 주소 수정을 위임하고 결과를 반환한다")
    void updateGoormAddress_delegatesToManager() {
        // given
        CreateAddressRequestDto requestDto = new CreateAddressRequestDto(UUID.randomUUID(), "address", "detail", "zip", "phone", "name");
        DeliveryAddress deliveryAddress = DeliveryAddress.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.updateGoormAddress(any(), any(), any(), any(), any())).thenReturn(deliveryAddress);

        // when
        DeliveryAddress result = deliveryService.updateGoormAddress(requestDto);

        // then
        assertThat(result).isEqualTo(deliveryAddress);
    }

    @Test
    @DisplayName("주문 ID로 배송 조회를 위임하고 결과를 반환한다")
    void getDeliveryByOrderId_delegatesToManager() {
        // given
        Delivery delivery = Delivery.builder().id(UUID.randomUUID()).build();
        when(deliveryManager.getDeliveryByOrderId(orderId)).thenReturn(delivery);

        // when
        Delivery result = deliveryService.getDeliveryByOrderId(orderId);

        // then
        assertThat(result).isEqualTo(delivery);
    }
}
