package profect.group1.goormdotcom.review.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;
import profect.group1.goormdotcom.common.apiPayload.ApiResponse;
import profect.group1.goormdotcom.review.infrastructure.client.dto.ObjectKeyResponseDto;

@FeignClient(
    name = "review-to-presigned",
    fallback = PresignedClientFallback.class,
    configuration = profect.group1.goormdotcom.common.config.FeignConfig.class
)
public interface PresignedClient {

    @PostMapping("/api/files/{fileId}/confirm")
    ApiResponse<Void> confirmUpload(@PathVariable("fileId") UUID fileId);

    @GetMapping("/api/files/{fileId}/url")
    ApiResponse<ObjectKeyResponseDto> getObjectKey(@PathVariable UUID fileId);
}

