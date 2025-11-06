package profect.group1.goormdotcom.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.common.apiPayload.exceptions.handler.OrderHandler;
import profect.group1.goormdotcom.order.infrastructure.client.DeliveryClient;
import profect.group1.goormdotcom.order.infrastructure.client.PaymentClient;
import profect.group1.goormdotcom.order.infrastructure.client.dto.DeliveryStartResponseDto;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentRequestDto;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentRequestItemDto;
import profect.group1.goormdotcom.order.infrastructure.client.dto.StockAdjustmentResponseDto;
import profect.group1.goormdotcom.order.infrastructure.client.StockClient;
import profect.group1.goormdotcom.order.infrastructure.kafka.service.StockKafkaProducer;
import profect.group1.goormdotcom.order.infrastructure.kafka.service.DeliveryKafkaProducer;
import profect.group1.goormdotcom.order.infrastructure.kafka.service.OrderCompletedKafkaProducer;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockDecreaseRequestMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.DeliveryRequestMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.OrderCompletedMessage;
import profect.group1.goormdotcom.order.domain.event.OrderCreatedEvent;
import profect.group1.goormdotcom.order.infrastructure.event.OrderEventPublisher;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderItemDto;
import profect.group1.goormdotcom.order.controller.external.v1.dto.OrderRequestDto;
import profect.group1.goormdotcom.order.domain.Order;
import profect.group1.goormdotcom.order.domain.enums.OrderStatus;
import profect.group1.goormdotcom.order.domain.mapper.OrderMapper; //?
import profect.group1.goormdotcom.order.repository.OrderAddressRepository;
import profect.group1.goormdotcom.order.repository.OrderProductRepository;
import profect.group1.goormdotcom.order.repository.OrderRepository;
import profect.group1.goormdotcom.order.repository.OrderStatusRepository;
import profect.group1.goormdotcom.order.repository.entity.OrderEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderAddressEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderProductEntity;
import profect.group1.goormdotcom.order.repository.entity.OrderStatusEntity;

@Slf4j
@Service
// @Transactional
@RequiredArgsConstructor
public class OrderService {

    // private static final String PaymentService;
    // private static final String Stock;
    // private static final String DelieveryService;
 
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final OrderMapper orderMapper;
    // private final StockRepository stockRepository;

    //Feign Clients
    private final StockClient stockClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    
    //Kafka Producers
    private final StockKafkaProducer stockKafkaProducer;
    private final DeliveryKafkaProducer deliveryKafkaProducer;
    private final OrderCompletedKafkaProducer orderCompletedKafkaProducer;
    
    //Event Publisher
    private final OrderEventPublisher eventPublisher;

    // @Value("${features.external-call.stock-check:true}")
    // private Boolean stockCheckEnabled;

    // @Value("${features.external-call.payment-check:true}")
    // private Boolean paymentCheckEnabled;

    // @Value("${features.external-call.delivery-check:true}")
    // private Boolean deliveryCheckEnabled;

    // @PersistenceContext
    // private EntityManager em;
       //상태 이력 추가
    private void appendOrderStatus(UUID orderId, OrderStatus status){   
        OrderEntity orderEntity = findOrderOrThrow(orderId);
        orderStatusRepository.save(OrderStatusEntity.builder()
            .order(orderEntity)
            .status(status.getCode())
            .build());
    }

    // 1) 주문 생성: 재고 선차감(예약) + 주문(PENDING) -> 주문 완료 하면 
    // 재고 확인 먼저
    @Transactional
    public Order create(UUID userId, OrderRequestDto req) {
        log.info("주문 생성 시작: userId={}, itemCount={}", userId, req.getItems().size());

        // 주문 생성 (상태: PENDING)
        OrderEntity orderEntity = OrderEntity.builder()
                        .customerId(userId)
                        .totalAmount(req.getTotalAmount())
                        .orderName(req.getOrderName())
                        .status(OrderStatus.PENDING.getCode())
                        .build();

        orderRepository.save(orderEntity);
        log.info("주문 엔티티 저장 완료: orderId={}, status=PENDING", orderEntity.getId());

        // 주문 상품 저장
        List<OrderProductEntity> lines = new ArrayList<>();
        for (OrderItemDto itemDto : req.getItems()) {
            String productName = (req.getOrderName() != null && !req.getOrderName().isBlank())
                    ? req.getOrderName() : "상품";
                    
            OrderProductEntity line = OrderProductEntity.builder()
                .order(orderEntity)
                .productId(itemDto.getProductId())
                .productName(productName)
                .quantity(itemDto.getQuantity())
                .totalAmount(req.getTotalAmount() / req.getItems().size())
                .build();
            lines.add(line);
        }
        orderProductRepository.saveAll(lines);

        // 상태 이력 추가
        appendOrderStatus(orderEntity.getId(), OrderStatus.PENDING);

        // 배송지 정보 저장
        OrderAddressEntity addressEntity = OrderAddressEntity.builder()
            .orderId(orderEntity.getId())
            .customerId(userId)
            .address(req.getAddress())
            .addressDetail(req.getAddressDetail())
            .zipcode(req.getZipcode())
            .phone(req.getPhone())
            .name(req.getName())
            .deliveryMemo(req.getDeliveryMemo())
            .build();
        orderAddressRepository.save(addressEntity);

        // 주문 생성 이벤트 발행 (이벤트 기반)
        List<OrderCreatedEvent.ProductItem> productItems = req.getItems().stream()
            .map(itemDto -> new OrderCreatedEvent.ProductItem(
                itemDto.getProductId(),
                itemDto.getQuantity()
            ))
            .collect(java.util.stream.Collectors.toList());
        
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
            orderEntity.getId(),
            userId,
            productItems
        );
        
        eventPublisher.publishOrderCreated(orderCreatedEvent);
        log.info("주문 생성 이벤트 발행 완료: orderId={}, productCount={}", 
            orderEntity.getId(), productItems.size());

        log.info("주문 생성 완료: orderId={}, orderName={}, status=PENDING", 
            orderEntity.getId(), orderEntity.getOrderName());

        return orderMapper.toDomain(orderEntity);
    }

    public Order completePayment(UUID orderId) {
        log.info("결제 완료 처리 시작: orderId={}", orderId);

        OrderEntity orderEntity = findOrderOrThrow(orderId);
        OrderAddressEntity addressEntity = orderAddressRepository.findByOrderId(orderId).orElseThrow(() -> new IllegalStateException("배송지 정보를 찾을 수 없습니다. orderId=" + orderId));

        // 배송 시작 
        try {
            ApiResponse<DeliveryStartResponseDto> response = deliveryClient.startDelivery(new DeliveryClient.StartDeliveryRequest(
                    orderId,
                    addressEntity.getCustomerId(),
                    addressEntity.getAddress(), addressEntity.getAddressDetail(),
                    addressEntity.getZipcode(), addressEntity.getPhone(), addressEntity.getName(),
                    addressEntity.getDeliveryMemo()
            ));

            if (!response.getCode().equals("COMMON200")) {
                log.error("배송 시작 실패: orderId={}, code={}, message={}",
                        orderId, response.getCode(), response.getMessage());
                appendOrderStatus(orderId, OrderStatus.CANCELLED);
                throw new IllegalStateException("배송 시작에 실패했습니다: " + response.getMessage());
            }
            log.info("배송 시작 완료: orderId={}, deliveryId={}", orderId, response.getResult());

        } catch (Exception e) {
            log.warn("[DELIVERY] 배달 생성 실패 - 주문은 결제완료로 유지: {}", e.getMessage());
            // TODO: 실패 건을 별도 테이블/큐에 적재하여 재시도
        }


        // 주문 상태 업데이트       
        appendOrderStatus(orderId, OrderStatus.COMPLETED);
        return orderMapper.toDomain(orderEntity);
    }

    /**
     * 결제 성공 처리 (Kafka Consumer에서 호출)
     * - 배송 요청 발행 (비동기)
     */
    @Transactional
    public void handlePaymentSuccess(UUID orderId) {
        log.info("결제 성공 처리 시작: orderId={}", orderId);

        OrderEntity orderEntity = findOrderOrThrow(orderId);
        OrderAddressEntity addressEntity = orderAddressRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalStateException("배송지 정보를 찾을 수 없습니다. orderId=" + orderId));

        // 배송 요청 발행 (비동기 - Kafka)
        DeliveryRequestMessage deliveryRequest = new DeliveryRequestMessage(
            orderId,
            addressEntity.getCustomerId(),
            addressEntity.getAddress(),
            addressEntity.getAddressDetail(),
            addressEntity.getZipcode(),
            addressEntity.getPhone(),
            addressEntity.getName(),
            addressEntity.getDeliveryMemo()
        );

        deliveryKafkaProducer.sendDeliveryRequest(deliveryRequest);
        log.info("배송 요청 발행 완료 (비동기): orderId={}", orderId);

        // 주문 상태 업데이트 (결제 완료)
        appendOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    /**
     * 결제 실패 처리 (Kafka Consumer에서 호출)
     * - 재고 복구
     * - 주문 상태 실패로 변경
     */
    @Transactional
    public void handlePaymentFailure(UUID orderId) {
        log.info("결제 실패 처리 시작: orderId={}", orderId);

        // 기존 failPayment 메서드 재사용
        failPayment(orderId);
    }

    /**
     * 배송 시작 처리 (Kafka Consumer에서 호출)
     * - 주문 완료 메시지 발행 (비동기)
     */
    @Transactional
    public void handleDeliveryStart(UUID orderId) {
        log.info("배송 시작 처리 시작: orderId={}", orderId);

        OrderEntity orderEntity = findOrderOrThrow(orderId);

        // 주문 완료 메시지 발행 (비동기 - Kafka)
        OrderCompletedMessage completedMessage = new OrderCompletedMessage(
            orderEntity.getId(),
            orderEntity.getCustomerId(),
            OrderStatus.COMPLETED.getCode(),
            orderEntity.getTotalAmount(),
            orderEntity.getOrderName()
        );

        orderCompletedKafkaProducer.sendOrderCompleted(completedMessage);
        log.info("주문 완료 메시지 발행 완료 (비동기): orderId={}, customerId={}", 
            orderId, orderEntity.getCustomerId());

        // 주문 상태는 이미 COMPLETED로 변경되어 있음
    }

    public Order failPayment(UUID orderId) {
        log.info("결제 실패 처리 시작: orderId={}", orderId);

        OrderEntity orderEntity = findOrderOrThrow(orderId);

        List<OrderProductEntity> products = orderProductRepository.findByOrderId(orderId);

        //재고 원복 요청 바디 구성
        StockAdjustmentRequestDto req = new StockAdjustmentRequestDto(
                products.stream()
                        .map(p -> new StockAdjustmentRequestItemDto(
                                p.getProductId(),
                                p.getQuantity()   //주문 시 차감했던 수량
                        ))
                        .toList()
        );

        ApiResponse<StockAdjustmentResponseDto> stockResponse = stockClient.increaseStock(req);

        if (stockResponse == null
                || stockResponse.getResult() == null
                || !stockResponse.getResult().status()) {
            log.error("재고 복구 실패: orderId={}, productIds={}",
                    orderId, products.stream().map(OrderProductEntity::getProductId).toList());
            throw new IllegalStateException("재고 복구에 실패했습니다.");
        }

        log.info("재고 복구 완료: orderId={}", orderId);

        //TODO:히스토리저장
        appendOrderStatus(orderId, OrderStatus.FAILED);
        return orderMapper.toDomain(orderEntity);
    }

    // 결제 취소 (배송전)
    public Order delieveryBefore(UUID orderId) {
        log.info("취소 처리 시작: orderId={}", orderId);
        
        OrderEntity orderEntity = findOrderOrThrow(orderId);


        // 취소 가능여부 확인
        ApiResponse<Integer> cancellableResponse = deliveryClient.checkCancellable(orderId);
        if (cancellableResponse.getResult() != 1) {
            log.error("취소 불가능: orderId={}", orderId);
            throw new IllegalStateException("취소 불가능합니다.");
        }

        
        //결제 취소 요청
        // TODO: paymentKey를 OrderEntity에 추가하여 주문 생성시 저장 필요
        // 현재는 orderId를 임시로 paymentKey로 사용
        PaymentClient.CancelResponse cancelResponse = paymentClient.cancelPayment(
            orderId, // TODO: 실제 paymentKey로 변경 필요
            "반품"
        );
        log.info("결제 취소 완료: paymentKey={}, status={}", 
            cancelResponse.paymentKey(), cancelResponse.status());
        
        // 재고 복구
        // FIXME: stock에서 정한 인터페이스에 맞추어 수정 필요 (251028 김현우)
        // List<OrderProductEntity> products = orderProductRepository.findAll().stream()
        //     .filter(p -> p.getOrder().getId().equals(orderId))
        //     .toList();
        
        // for (OrderProductEntity product : products) {
        //     ApiResponse<StockAdjustmentResponseDto> stockResponse = stockClient.increaseStock(product.getProductId(), product.getQuantity());
        //     if (!stockResponse.getResult().status()) {
        //         log.error("재고 복구 실패: orderId={}, productId={}", orderId, product.getProductId());
        //         throw new IllegalStateException("재고 복구에 실패했습니다. productId=" + product.getProductId());
        //     }
        // }
        // log.info("재고 복구 완료: orderId={}", orderId);

        //배송 취소 요청
        deliveryClient.cancelDelivery(new DeliveryClient.CancelDeliveryRequest(orderId));


        log.info("배송 취소 완료: orderId={}", orderId);
        // 상태 업데이트
        appendOrderStatus(orderId, OrderStatus.CANCELLED);
        log.info("주문 취소 처리 완료: orderId={}", orderId);
        return orderMapper.toDomain(orderEntity);
    }

    // 취소 로직(반송)
    public Order cancel(UUID orderId) {
        OrderEntity order = findOrderOrThrow(orderId);

        // 취소 가능여부 확인
        ApiResponse<Integer> cancellableResponse = deliveryClient.checkCancellable(orderId);
        if (cancellableResponse.getResult() != 2) {
            log.error("반송 불가능: orderId={}", orderId);
            throw new IllegalStateException("반송 불가능합니다.");
        }

        // 반송 요청
        deliveryClient.requestReturn(new DeliveryClient.ReturnDeliveryRequest(orderId));

        //결제 취소 요청
        // TODO: paymentKey를 OrderEntity에 추가하여 주문 생성시 저장 필요
        // 현재는 orderId를 임시로 paymentKey로 사용
        PaymentClient.CancelResponse cancelResponse = paymentClient.cancelPayment(
            orderId,
            "반품"
        );
        log.info("결제 취소 완료: paymentKey={}, status={}", 
            cancelResponse.paymentKey(), cancelResponse.status());
        appendOrderStatus(orderId, OrderStatus.CANCELLED);
        return orderMapper.toDomain(order);
    }
    //단건 조회 최신
    @Transactional(readOnly = true)
    public Order getOne(UUID id) {
        OrderEntity e = findOrderOrThrow(id);
        OrderStatusEntity current = orderStatusRepository.findTop1ByOrder_IdOrderByCreatedAtDesc(id)
        .orElseThrow(() -> new IllegalStateException("상태 이력이 없습니다. orderId=" + id));
    // // return OrderDtoMapper.toResponseDto(e, current.getStatus().getCode(), current.getStatus().getCode());
        return  orderMapper.toDomain(e);
    }
    //전체 조회
    @Transactional(readOnly = true)
    public List<Order> getAll() {
        List<OrderEntity> entities = orderRepository.findAll();
        return entities.stream()
            .map(e -> {OrderStatusEntity current = orderStatusRepository
                .findTop1ByOrder_IdOrderByCreatedAtDesc(e.getId())
                .orElse(null);
                return  orderMapper.toDomain(e);})
            .toList();
    }
 
    //최신 상태 조회
    @Transactional(readOnly = true)
    private OrderStatusEntity latestStatus(UUID orderId){
        return orderStatusRepository
            .findTop1ByOrder_IdOrderByCreatedAtDesc(orderId)
            .orElseThrow(() -> new IllegalStateException("상태 이력이 없음. orderId=" + orderId));
    }
    
    //주문 조회
    private OrderEntity findOrderOrThrow(UUID id){
        return orderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + id));
    }

    // userId + productId로 주문 조회
    public UUID getOrderIdByUserAndProduct(UUID customerId, UUID productId) {
        return orderRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."))
                .getId();
    }

    @Transactional
    public void deliveryReturnCompleted(UUID orderId) {
        OrderEntity order = findOrderOrThrow(orderId);

        if(order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new IllegalStateException("이미 반송된 주문입니다.");
        }

        appendOrderStatus(orderId, OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
}