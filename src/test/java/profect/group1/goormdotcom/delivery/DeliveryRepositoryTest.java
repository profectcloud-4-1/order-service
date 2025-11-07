package profect.group1.goormdotcom.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import profect.group1.goormdotcom.common.config.JpaAuditingConfig;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryReturnStepType;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStatus;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStepType;
import profect.group1.goormdotcom.delivery.repository.*;
import profect.group1.goormdotcom.delivery.repository.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class DeliveryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private DeliveryReturnRepository deliveryReturnRepository;

    @Autowired
    private DeliveryReturnAddressRepository deliveryReturnAddressRepository;

    @Autowired
    private DeliveryStepHistoryRepository deliveryStepHistoryRepository;

    @Autowired
    private DeliveryReturnStepHistoryRepository deliveryReturnStepHistoryRepository;

    @Autowired
    private GoormAddressRepository goormAddressRepository;

    @Test
    @DisplayName("DeliveryRepository - 주문 ID로 배송 찾기 테스트")
    void findByOrderId_Delivery() {
        // given
        UUID orderId = UUID.randomUUID();
        DeliveryEntity deliveryEntity = DeliveryEntity.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .status(DeliveryStatus.PENDING.getCode())
                .build();
        entityManager.persist(deliveryEntity);
        entityManager.flush();

        // when
        Optional<DeliveryEntity> foundDelivery = deliveryRepository.findByOrderId(orderId);

        // then
        assertThat(foundDelivery).isPresent();
        assertThat(foundDelivery.get().getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("DeliveryAddressRepository - 배송 ID로 주소 찾기 테스트")
    void findByDeliveryId_DeliveryAddress() {
        // given
        DeliveryEntity deliveryEntity = DeliveryEntity.builder()
                .orderId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(DeliveryStatus.PENDING.getCode())
                .build();
        entityManager.persist(deliveryEntity);

        DeliveryAddressEntity addressEntity = DeliveryAddressEntity.builder()
                .deliveryId(deliveryEntity.getId())
                .senderAddress("sender")
                .senderAddressDetail("sender detail")
                .senderZipcode("123")
                .senderPhone("123")
                .senderName("sender")
                .receiverAddress("receiver")
                .receiverAddressDetail("receiver detail")
                .receiverZipcode("456")
                .receiverPhone("456")
                .receiverName("receiver")
                .deliveryMemo("memo")
                .build();
        entityManager.persist(addressEntity);
        entityManager.flush();

        // when
        Optional<DeliveryAddressEntity> foundAddress = deliveryAddressRepository.findByDeliveryId(deliveryEntity.getId());

        // then
        assertThat(foundAddress).isPresent();
        assertThat(foundAddress.get().getDeliveryId()).isEqualTo(deliveryEntity.getId());
    }

    @Test
    @DisplayName("DeliveryStepHistoryRepository - 배송 ID로 단계 이력 찾기 테스트")
    void findAllByDeliveryIdOrderByCreatedAtDesc_DeliveryStepHistory() {
        // given
        DeliveryEntity deliveryEntity = DeliveryEntity.builder()
                .orderId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(DeliveryStatus.PENDING.getCode())
                .build();
        entityManager.persist(deliveryEntity);

        DeliveryStepHistoryEntity step1 = DeliveryStepHistoryEntity.builder()
                .deliveryId(deliveryEntity.getId())
                .stepType(DeliveryStepType.INIT.getCode())
                .build();
        entityManager.persist(step1);
        entityManager.flush();

        // when
        List<DeliveryStepHistoryEntity> foundSteps = deliveryStepHistoryRepository.findAllByDeliveryIdOrderByCreatedAtDesc(deliveryEntity.getId());

        // then
        assertThat(foundSteps).hasSize(1);
    }

    @Test
    @DisplayName("DeliveryReturnRepository - 반품 생성 테스트")
    void save_DeliveryReturn() {
        // given
        DeliveryEntity deliveryEntity = DeliveryEntity.builder()
                .orderId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(DeliveryStatus.PENDING.getCode())
                .build();
        entityManager.persist(deliveryEntity);

        DeliveryReturnEntity returnEntity = DeliveryReturnEntity.builder()
                .deliveryId(deliveryEntity.getId())
                .build();

        // when
        DeliveryReturnEntity savedReturn = deliveryReturnRepository.save(returnEntity);

        // then
        assertThat(savedReturn).isNotNull();
        assertThat(savedReturn.getDeliveryId()).isEqualTo(deliveryEntity.getId());
    }

    @Test
    @DisplayName("DeliveryReturnAddressRepository - 반품 주소 생성 테스트")
    void save_DeliveryReturnAddress() {
        // given
        DeliveryReturnEntity returnEntity = DeliveryReturnEntity.builder()
                .deliveryId(UUID.randomUUID())
                .build();
        entityManager.persist(returnEntity);

        DeliveryReturnAddressEntity returnAddressEntity = DeliveryReturnAddressEntity.builder()
                .deliveryReturnId(returnEntity.getId())
                .build();

        // when
        DeliveryReturnAddressEntity savedReturnAddress = deliveryReturnAddressRepository.save(returnAddressEntity);

        // then
        assertThat(savedReturnAddress).isNotNull();
        assertThat(savedReturnAddress.getDeliveryReturnId()).isEqualTo(returnEntity.getId());
    }

    @Test
    @DisplayName("DeliveryReturnStepHistoryRepository - 반품 단계 이력 생성 테스트")
    void save_DeliveryReturnStepHistory() {
        // given
        DeliveryReturnEntity returnEntity = DeliveryReturnEntity.builder()
                .deliveryId(UUID.randomUUID())
                .build();
        entityManager.persist(returnEntity);

        DeliveryReturnStepHistoryEntity returnStepHistoryEntity = DeliveryReturnStepHistoryEntity.builder()
                .deliveryReturnId(returnEntity.getId())
                .stepType(DeliveryReturnStepType.INIT.getCode())
                .build();

        // when
        DeliveryReturnStepHistoryEntity savedReturnStepHistory = deliveryReturnStepHistoryRepository.save(returnStepHistoryEntity);

        // then
        assertThat(savedReturnStepHistory).isNotNull();
        assertThat(savedReturnStepHistory.getDeliveryReturnId()).isEqualTo(returnEntity.getId());
    }

    @Test
    @DisplayName("GoormAddressRepository - 최신 구름 주소 찾기 테스트")
    void findTopByOrderByCreatedAtDesc_GoormAddress() {
        // given
        GoormAddressEntity address1 = GoormAddressEntity.builder().address("address1").addressDetail("detail1").zipcode("1").phone("1").name("1").build();
        entityManager.persist(address1);
        entityManager.flush();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GoormAddressEntity address2 = GoormAddressEntity.builder().address("address2").addressDetail("detail2	").zipcode("2").phone("2").name("2").build();
        entityManager.persist(address2);
        entityManager.flush();

        // when
        Optional<GoormAddressEntity> foundAddress = goormAddressRepository.findTopByOrderByCreatedAtDesc();

        // then
        assertThat(foundAddress).isPresent();
        assertThat(foundAddress.get().getAddress()).isEqualTo("address2");
    }
}
