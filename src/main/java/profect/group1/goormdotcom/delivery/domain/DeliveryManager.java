package profect.group1.goormdotcom.delivery.domain;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryStepHistoryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnStepHistoryRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryEntity;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryMapper;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStatus;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryStepType;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryAddressEntity;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryStepHistoryEntity;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryReturnEntity;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryReturnAddressEntity;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryReturnStepHistoryEntity;
import profect.group1.goormdotcom.delivery.repository.entity.GoormAddressEntity;
import profect.group1.goormdotcom.delivery.repository.entity.CustomerAddressEntity;
import profect.group1.goormdotcom.delivery.repository.GoormAddressRepository;
import profect.group1.goormdotcom.delivery.repository.CustomerAddressRepository;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryAddressMapper;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryStepHistoryMapper;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryReturnStatus;
import profect.group1.goormdotcom.delivery.domain.enums.DeliveryReturnStepType;
import profect.group1.goormdotcom.delivery.domain.DeliveryReturn;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryReturnMapper;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryReturnAddressMapper;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryManager {

	private final DeliveryRepository repo;
	private final DeliveryReturnRepository returnRepo;
	private final DeliveryAddressRepository addressRepo;
	private final GoormAddressRepository goormAddressRepo;
	private final CustomerAddressRepository customerAddressRepo;
	private final DeliveryReturnAddressRepository returnAddressRepo;
	private final DeliveryStepHistoryRepository stepHistoryRepo;
	private final DeliveryReturnStepHistoryRepository returnStepHistoryRepo;
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final DeliveryReturnAddressMapper deliveryReturnAddressMapper;
    private final DeliveryStepHistoryMapper deliveryStepHistoryMapper;
    private final PlatformTransactionManager transactionManager;

    public List<DeliveryAddress> getAddressesByCustomerId(UUID customerId) {
        List<CustomerAddressEntity> entities = this.customerAddressRepo.findAllByCustomerId(customerId);
        return entities.stream().map(this.deliveryAddressMapper::toDomainFromCustomerAddress).toList();
    }

    public Delivery getDeliveryByOrderId(UUID orderId) {
        DeliveryEntity entity = this.repo.findByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        DeliveryAddressEntity addressEntity = this.addressRepo.findByDeliveryId(entity.getId()).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        DeliveryAddress senderAddress = this.deliveryAddressMapper.toDomainOfSender(addressEntity);
        DeliveryAddress receiverAddress = this.deliveryAddressMapper.toDomainOfReceiver(addressEntity);

        List<DeliveryStepHistory> stepHistories = this.stepHistoryRepo.findAllByDeliveryIdOrderByCreatedAtDesc(entity.getId()).stream().map(this.deliveryStepHistoryMapper::toDomain).toList();

        Delivery delivery = Delivery.builder()
            .id(entity.getId())
            .orderId(entity.getOrderId())
            .status(entity.getStatus())
            .trackingNumber(entity.getTrackingNumber())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .senderAddress(senderAddress)
            .receiverAddress(receiverAddress)
            .deliveryStepHistories(stepHistories)
            .build();
        return delivery;
    }

	@Transactional
    public Delivery createDelivery(final UUID orderId, final UUID customerAddressId) {
        Optional<DeliveryEntity> already = this.repo.findByOrderId(orderId);
        if (already.isPresent()) throw new IllegalArgumentException("Delivery already exists");

        GoormAddressEntity goormAddressEntity = this.goormAddressRepo.findTopByOrderByCreatedAtDesc().orElseThrow(() -> new IllegalArgumentException("Goorm address not found"));
        CustomerAddressEntity customerAddressEntity = this.customerAddressRepo.findById(customerAddressId).orElseThrow(() -> new IllegalArgumentException("Customer address not found"));

        // insert delivery
        DeliveryEntity deliveryEntity = DeliveryEntity.builder()
            .orderId(orderId)
            .status(DeliveryStatus.PENDING.getCode())
            .build();
        
        this.repo.save(deliveryEntity);
        UUID deliveryId = deliveryEntity.getId();
        Delivery delivery = DeliveryMapper.toDomain(deliveryEntity);

        // insert address
        DeliveryAddressEntity address = DeliveryAddressEntity.builder()
            .deliveryId(deliveryId)
            .senderAddress(goormAddressEntity.getAddress())
            .senderAddressDetail(goormAddressEntity.getAddressDetail())
            .senderZipcode(goormAddressEntity.getZipcode())
            .senderPhone(goormAddressEntity.getPhone())
            .senderName(goormAddressEntity.getName())
            .receiverAddress(customerAddressEntity.getAddress())
            .receiverAddressDetail(customerAddressEntity.getAddressDetail())
            .receiverZipcode(customerAddressEntity.getZipcode())
            .receiverPhone(customerAddressEntity.getPhone())
            .receiverName(customerAddressEntity.getName())
            .build();
        this.addressRepo.save(address);

        delivery.setSenderAddress(this.deliveryAddressMapper.toDomainOfSender(address));
        delivery.setReceiverAddress(this.deliveryAddressMapper.toDomainOfReceiver(address));

        // insert step history
        DeliveryStepHistoryEntity stepHistoryEntity = DeliveryStepHistoryEntity.builder()
            .deliveryId(deliveryId)
            .stepType(DeliveryStepType.INIT.getCode())
            .build();
        this.stepHistoryRepo.save(stepHistoryEntity);

        // NOTE: 실제 배송시스템 구현 대신 30초마다 배송단계 자동진행.
        // (INIT) -> READY -> HUB -> STARTED -> DONE
        scheduleStepProgression(deliveryId, Arrays.asList(
            DeliveryStepType.READY,
            DeliveryStepType.HUB,
            DeliveryStepType.STARTED,
            DeliveryStepType.DONE
        ));

        return delivery;
    }

    @Transactional
    public void cancel(final UUID deliveryId) {
        Optional<DeliveryEntity> entity = this.repo.findById(deliveryId);
        if (entity.isEmpty()) throw new IllegalArgumentException("Delivery not found");

        Delivery delivery = DeliveryMapper.toDomain(entity.get());
        if (delivery.canReturn() != 1) throw new IllegalArgumentException("Delivery cannot be cancelled");
        
        entity.get().setStatus(DeliveryStatus.CANCELLED.getCode());
        this.repo.save(entity.get());
    }

    @Transactional
    public DeliveryReturn returnDelivery(final UUID deliveryId) {
        DeliveryEntity entity = this.repo.findById(deliveryId).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        Delivery delivery = DeliveryMapper.toDomain(entity);
        if (delivery.canReturn() != 2) throw new IllegalArgumentException("Delivery cannot be returned");

        DeliveryAddressEntity deliveryAddressEntity = this.addressRepo.findByDeliveryId(deliveryId).orElseThrow(() -> new IllegalArgumentException("Delivery address not found"));

        // insert delivery_return
        DeliveryReturnEntity returnEntity = DeliveryReturnEntity.builder()
            .deliveryId(deliveryId)
            .status(DeliveryReturnStatus.PENDING.getCode())
            .build();
        this.returnRepo.save(returnEntity);

        UUID returnId = returnEntity.getId();
        DeliveryReturn deliveryReturn = DeliveryReturnMapper.toDomain(returnEntity);

        // insert address
        // 발신: CUSTOMER Address (배송에서의 receiver address), 수신: BRAND Address (배송에서의 sender address)
        DeliveryReturnAddressEntity address = DeliveryReturnAddressEntity.builder()
            .deliveryReturnId(returnId)
            .senderAddress(deliveryAddressEntity.getReceiverAddress())
            .senderAddressDetail(deliveryAddressEntity.getReceiverAddressDetail())
            .senderZipcode(deliveryAddressEntity.getReceiverZipcode())
            .senderPhone(deliveryAddressEntity.getReceiverPhone())
            .senderName(deliveryAddressEntity.getReceiverName())
            .receiverAddress(deliveryAddressEntity.getSenderAddress())
            .receiverAddressDetail(deliveryAddressEntity.getSenderAddressDetail())
            .receiverZipcode(deliveryAddressEntity.getSenderZipcode())
            .receiverPhone(deliveryAddressEntity.getSenderPhone())
            .receiverName(deliveryAddressEntity.getSenderName())
            .build();
        this.returnAddressRepo.save(address);

        deliveryReturn.setSenderAddress(this.deliveryReturnAddressMapper.toDomainOfSender(address));
        deliveryReturn.setReceiverAddress(this.deliveryReturnAddressMapper.toDomainOfReceiver(address));

        // insert step history
        DeliveryReturnStepHistoryEntity stepHistoryEntity = DeliveryReturnStepHistoryEntity.builder()
            .deliveryReturnId(returnId)
            .stepType(DeliveryReturnStepType.INIT.getCode())
            .build();
        this.returnStepHistoryRepo.save(stepHistoryEntity);

        // NOTE: 실제 배송시스템 구현 대신 30초마다 반송단계 자동진행.
        // (INIT) -> STARTED -> HUB -> STARTED_HUB -> DONE
        scheduleReturnStepProgression(returnId, Arrays.asList(
            DeliveryReturnStepType.STARTED,
            DeliveryReturnStepType.HUB,
            DeliveryReturnStepType.STARTED_HUB,
            DeliveryReturnStepType.DONE
        ));

        return deliveryReturn;
    }

    public DeliveryAddress createCustomerAddress(UUID customerId, String address, String addressDetail, String zipcode, String phone, String name) {
        CustomerAddressEntity entity = CustomerAddressEntity.builder()
            .customerId(customerId)
            .address(address)
            .addressDetail(addressDetail)
            .zipcode(zipcode)
            .phone(phone)
            .name(name)
            .build();
        this.customerAddressRepo.save(entity);
        return this.deliveryAddressMapper.toDomainFromCustomerAddress(entity);
    }

    public DeliveryAddress updateCustomerAddress(UUID customerId, UUID addressId, String address, String addressDetail, String zipcode, String phone, String name) {
        CustomerAddressEntity entity = this.customerAddressRepo.findById(addressId)
            .orElseThrow(() -> new IllegalArgumentException("Customer address not found"));
        if (!entity.getCustomerId().equals(customerId)) throw new IllegalArgumentException("Forbidden: not your address");
        entity.setAddress(address);
        entity.setAddressDetail(addressDetail);
        entity.setZipcode(zipcode);
        entity.setPhone(phone);
        entity.setName(name);
        this.customerAddressRepo.save(entity);
        return this.deliveryAddressMapper.toDomainFromCustomerAddress(entity);
    }

    public void deleteCustomerAddress(UUID customerId, UUID addressId) {
        CustomerAddressEntity entity = this.customerAddressRepo.findById(addressId)
            .orElseThrow(() -> new IllegalArgumentException("Customer address not found"));
        if (!entity.getCustomerId().equals(customerId)) throw new IllegalArgumentException("Forbidden: not your address");
        this.customerAddressRepo.delete(entity);
    }

    // ===== Goormdotcom Address (MASTER) =====
    public DeliveryAddress getGoormAddress() {
        GoormAddressEntity entity = this.goormAddressRepo.findTopByOrderByCreatedAtDesc().orElseThrow(() -> new IllegalArgumentException("Goorm address not found"));
        return this.deliveryAddressMapper.toDomainFromGoormAddress(entity);
    }

    public DeliveryAddress createGoormAddress(String address, String addressDetail, String zipcode, String phone, String name) {
        Optional<GoormAddressEntity> already = this.goormAddressRepo.findTopByOrderByCreatedAtDesc();
        if (already.isPresent()) throw new IllegalArgumentException("Goorm address already exists");

        GoormAddressEntity entity = GoormAddressEntity.builder()
            .address(address)
            .addressDetail(addressDetail)
            .zipcode(zipcode)
            .phone(phone)
            .name(name)
            .build();
        this.goormAddressRepo.save(entity);
        return this.deliveryAddressMapper.toDomainFromGoormAddress(entity);
    }

    public DeliveryAddress updateGoormAddress(String address, String addressDetail, String zipcode, String phone, String name) {
        GoormAddressEntity entity = this.goormAddressRepo.findTopByOrderByCreatedAtDesc()
            .orElseThrow(() -> new IllegalArgumentException("Goorm address not found"));
        entity.setAddress(address);
        entity.setAddressDetail(addressDetail);
        entity.setZipcode(zipcode);
        entity.setPhone(phone);
        entity.setName(name);
        this.goormAddressRepo.save(entity);
        return this.deliveryAddressMapper.toDomainFromGoormAddress(entity);
    }

    private String generateTrackingNumber() {
        // 단순 송장번호 생성 로직 (운영 환경에서는 택배사 연동 필요)
        return "TRK-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private void scheduleStepProgression(final UUID deliveryId, final List<DeliveryStepType> steps) {
        final UUID targetDeliveryId = deliveryId;
        for (int i = 0; i < steps.size(); i++) {
            final int delayMinutes = i + 1; // 첫 스텝은 1분 후
            final DeliveryStepType stepType = steps.get(i);
            CompletableFuture.delayedExecutor(delayMinutes * 30, TimeUnit.SECONDS).execute(() -> {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    this.repo.findById(targetDeliveryId).ifPresent(delivery -> {
                        // READY 시점에만 상태 전환 및 송장번호 발급
                        if (stepType == DeliveryStepType.READY) {
                            // 중복 전환 방지: 아직 PENDING 상태일 때만
                            if (!DeliveryStatus.PENDING.getCode().equals(delivery.getStatus())) return;
                            delivery.setTrackingNumber(generateTrackingNumber());
                            delivery.setStatus(DeliveryStatus.IN_DELIVERY.getCode());
                            this.repo.save(delivery);
                        }

                        if (stepType == DeliveryStepType.DONE) {
                            delivery.setStatus(DeliveryStatus.FINISH.getCode());
                            this.repo.save(delivery);
                            // TODO: order에 배송완료 통보
                        }


                        DeliveryStepHistoryEntity history = DeliveryStepHistoryEntity.builder()
                            .deliveryId(targetDeliveryId)
                            .stepType(stepType.getCode())
                            .build();
                        this.stepHistoryRepo.save(history);

                    });
                });
            });
        }
    }

    private void scheduleReturnStepProgression(final UUID deliveryReturnId, final List<DeliveryReturnStepType> steps) {
        final UUID targetReturnId = deliveryReturnId;
        for (int i = 0; i < steps.size(); i++) {
            final int delayIndex = i + 1; // 첫 스텝은 30초 후
            final DeliveryReturnStepType stepType = steps.get(i);
            CompletableFuture.delayedExecutor(delayIndex * 30, TimeUnit.SECONDS).execute(() -> {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    this.returnRepo.findById(targetReturnId).ifPresent(ret -> {
                        // STARTED 시점에만 상태 전환 및 송장번호 발급
                        if (stepType == DeliveryReturnStepType.STARTED) {
                            if (!DeliveryReturnStatus.PENDING.getCode().equals(ret.getStatus())) return;
                            ret.setTrackingNumber(generateTrackingNumber());
                            ret.setStatus(DeliveryReturnStatus.IN_DELIVERY.getCode());
                            this.returnRepo.save(ret);
                        }

                        if (stepType == DeliveryReturnStepType.DONE) {
                            ret.setStatus(DeliveryReturnStatus.FINISH.getCode());
                            this.returnRepo.save(ret);
                            // TODO: order에 반송완료 통보
                        }

                        DeliveryReturnStepHistoryEntity history = DeliveryReturnStepHistoryEntity.builder()
                            .deliveryReturnId(targetReturnId)
                            .stepType(stepType.getCode())
                            .build();
                        this.returnStepHistoryRepo.save(history);
                    });
                });
            });
        }
    }

}