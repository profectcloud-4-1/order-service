package profect.group1.goormdotcom.product.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;
import profect.group1.goormdotcom.product.controller.dto.ProductRequestDto;
import profect.group1.goormdotcom.product.controller.dto.ProductResponseDto;
import profect.group1.goormdotcom.product.controller.mapper.ProductDtoMapper;
import profect.group1.goormdotcom.product.domain.Product;
import profect.group1.goormdotcom.product.service.ProductService;
import profect.group1.goormdotcom.user.presentation.auth.LoginUser;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductController {
    private final ProductService productService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<UUID> registerProduct(
        @RequestBody @Valid ProductRequestDto request
    ) {
        UUID productId = productService.createProduct(
            request.brandId(), request.categoryId(), request.name(), request.price(), request.description()
        ); 
        return ApiResponse.of(SuccessStatus._OK, productId);
    }
    
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponseDto> getProduct(
        @PathVariable(value = "productId") UUID productId
    ) {
        Product product = productService.getProduct(productId);
        return ApiResponse.of(SuccessStatus._OK, ProductDtoMapper.toProductResponseDto(product));
    }
    
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductResponseDto> updateProduct(
        @PathVariable(value = "productId") UUID productId, 
        @RequestBody @Valid ProductRequestDto request
    ) {
        Product product = productService.updateProduct(
            productId,
            request.categoryId(),
            request.name(),
            request.price(),
            request.description()
        );
        
        return ApiResponse.of(SuccessStatus._OK, ProductDtoMapper.toProductResponseDto(product));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<UUID> deleteProduct(
        @PathVariable(value = "productId") UUID productId
    )  {
        productService.deleteProduct(productId);
        return ApiResponse.of(SuccessStatus._OK, productId);
    }

}
