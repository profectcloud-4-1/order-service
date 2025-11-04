package profect.group1.goormdotcom.review.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.review.infrastructure.client.dto.ObjectKeyResponseDto;

@Slf4j
@Component("reviewPresignedClientFallback")
public class PresignedClientFallback implements PresignedClient {
    
    @Override
    public ApiResponse<Void> confirmUpload(UUID fileId) {
        log.error("[Feign-Fallback] Presigned-service confirmUpload failed. fileId: {}", fileId);
        return ApiResponse.onFailure(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "Presigned 서비스가 일시적으로 사용할 수 없습니다.",
            null
        );
    }

    @Override
    public ApiResponse<ObjectKeyResponseDto> getObjectKey(UUID fileId) {
        log.error("[Feign-Fallback] Presigned-service getObjectKey failed. fileId: {}", fileId);
        return ApiResponse.onFailure(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "Presigned 서비스가 일시적으로 사용할 수 없습니다.",
            null
        );
    }
}

