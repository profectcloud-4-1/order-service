package profect.group1.goormdotcom.payment.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import profect.group1.goormdotcom.payment.controller.dto.PaymentResponseDto;
import profect.group1.goormdotcom.payment.controller.dto.request.PaymentCreateRequestDto;
import profect.group1.goormdotcom.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "결제 관리", description = "결제 관련 API")
public interface PaymentApiDocs {
    @Operation(
            summary = "결제 요청 API",
            description = "사용자가 주문에 대해 결제를 요청하면 새로운 결제가 생성되고 상태는 PENDING으로 설정됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공입니다"),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 (예: 1000원 미만 결제)",
                    content = @Content(schema = @Schema(implementation = profect.group1.goormdotcom.apiPayload.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = profect.group1.goormdotcom.apiPayload.ApiResponse.class))
            )


    })
    profect.group1.goormdotcom.apiPayload.ApiResponse<PaymentResponseDto> requestPayment(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid PaymentCreateRequestDto paymentRequestDto
    );
}
