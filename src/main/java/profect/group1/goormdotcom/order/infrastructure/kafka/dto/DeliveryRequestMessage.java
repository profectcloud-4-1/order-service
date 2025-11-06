package profect.group1.goormdotcom.order.infrastructure.kafka.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 배송 요청 메시지 (Order -> Delivery)
 */
public record DeliveryRequestMessage(
    @JsonProperty("orderId")
    UUID orderId,
    
    @JsonProperty("customerId")
    UUID customerId,
    
    @JsonProperty("address")
    String address,
    
    @JsonProperty("addressDetail")
    String addressDetail,
    
    @JsonProperty("zipcode")
    String zipcode,
    
    @JsonProperty("phone")
    String phone,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("deliveryMemo")
    String deliveryMemo
) {}

