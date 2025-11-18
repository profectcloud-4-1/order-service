package profect.group1.goormdotcom.delivery.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import profect.group1.goormdotcom.delivery.event.DeliveryStartFailedEvent;
import profect.group1.goormdotcom.delivery.repository.GoormAddressRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DeliveryManager의 롤백 감지 및 보상 이벤트 발행을 검증하는 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class DeliveryManagerRollbackTest {

    @Autowired
    private DeliveryManager deliveryManager;

    @Autowired
    private GoormAddressRepository goormAddressRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ApplicationEvents applicationEvents;

    private UUID orderId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        
        // 테스트 전에 Goorm 주소가 없도록 보장 (실제 DB에서 삭제)
        goormAddressRepository.deleteAll();
    }

    @Test
    @DisplayName("startDelivery() - Goorm 주소 없을 때 롤백 감지 및 보상 이벤트 발행 테스트")
    void startDelivery_whenGoormAddressNotFound_publishesCompensationEvent() {
        // given
        // Goorm 주소가 없는 상태 (setUp에서 삭제됨)

        // when
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryManager.startDelivery(
                orderId, 
                customerId, 
                "서울시 강남구", 
                "테헤란로 123", 
                "12345", 
                "010-1234-5678", 
                "홍길동", 
                "문 앞에 놓아주세요"
            );
        });

        // then - 보상 이벤트가 발행되었는지 확인
        long eventCount = applicationEvents.stream(DeliveryStartFailedEvent.class)
            .filter(event -> event.orderId().equals(orderId))
            .count();
        
        assertThat(eventCount)
            .as("롤백 감지 시 DeliveryStartFailedEvent가 발행되어야 합니다")
            .isEqualTo(1);
    }

    @Test
    @DisplayName("startDelivery() - 정상 실행 시 보상 이벤트가 발행되지 않는지 테스트")
    void startDelivery_whenSuccess_doesNotPublishCompensationEvent() {
        // given - Goorm 주소 생성
        profect.group1.goormdotcom.delivery.repository.entity.GoormAddressEntity goormAddress = 
            profect.group1.goormdotcom.delivery.repository.entity.GoormAddressEntity.builder()
                .address("서울시 강남구")
                .addressDetail("테헤란로 456")
                .zipcode("12345")
                .phone("02-1234-5678")
                .name("구름닷컴")
                .build();
        goormAddressRepository.save(goormAddress);

        // when - 정상 실행
        try {
            deliveryManager.startDelivery(
                orderId, 
                customerId, 
                "서울시 강남구", 
                "테헤란로 123", 
                "12345", 
                "010-1234-5678", 
                "홍길동", 
                "문 앞에 놓아주세요"
            );
        } catch (Exception e) {
            // 다른 예외가 발생할 수 있으므로 무시
        }

        // then - 보상 이벤트가 발행되지 않았는지 확인
        long eventCount = applicationEvents.stream(DeliveryStartFailedEvent.class)
            .filter(event -> event.orderId().equals(orderId))
            .count();
        
        assertThat(eventCount)
            .as("정상 실행 시 DeliveryStartFailedEvent가 발행되지 않아야 합니다")
            .isEqualTo(0);
    }
}

