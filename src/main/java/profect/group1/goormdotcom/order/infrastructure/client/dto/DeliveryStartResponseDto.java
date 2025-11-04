package profect.group1.goormdotcom.order.infrastructure.client.dto;

import java.util.UUID;

public record DeliveryStartResponseDto(
        UUID id,
        UUID orderId,
        String status,
        String trackingNumber,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
) {}

