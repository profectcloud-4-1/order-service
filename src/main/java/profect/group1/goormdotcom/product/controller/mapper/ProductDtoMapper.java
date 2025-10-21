package profect.group1.goormdotcom.product.controller.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import profect.group1.goormdotcom.product.controller.dto.ProductResponseDto;
import profect.group1.goormdotcom.product.domain.Product;
import profect.group1.goormdotcom.product.domain.ProductImage;

@Component
public class ProductDtoMapper {
    
    public static ProductResponseDto toProductResponseDto(Product product) {
        return new ProductResponseDto(
            product.getName(), 
            product.getBrandId(), 
            product.getCategoryId(), 
            product.getDescription(),
            product.getPrice(), 
            // TODO: url로 제공. 현재는 imageId string으로 제공
            product.getImages().stream().map(ProductImage::getId).toList()
                            .stream().map(UUID::toString).toList()
        );
    }
}
