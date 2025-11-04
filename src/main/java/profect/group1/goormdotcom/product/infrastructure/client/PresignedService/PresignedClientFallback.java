package profect.group1.goormdotcom.product.infrastructure.client.PresignedService;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.product.infrastructure.client.PresignedService.dto.ObjectKeyResponse;

@Component("productPresignedClientFallback")
public class PresignedClientFallback implements PresignedClient {
    
    @Override
    public ApiResponse<Void> confirmUpload(UUID fileId) {
        return ApiResponse.onFailure(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "Presigned 서비스가 일시적으로 사용할 수 없습니다.",
            null
        );
    }

    @Override
    public ApiResponse<ObjectKeyResponse> getObjectKey(UUID fileId) {
        return ApiResponse.onFailure(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "Presigned 서비스가 일시적으로 사용할 수 없습니다.",
            null
        );
    }
}
