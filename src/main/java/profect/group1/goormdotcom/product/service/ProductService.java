package profect.group1.goormdotcom.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.product.domain.Product;
import profect.group1.goormdotcom.product.domain.ProductImage;
import profect.group1.goormdotcom.product.repository.ProductImageRepository;
import profect.group1.goormdotcom.product.repository.ProductRepository;
import profect.group1.goormdotcom.product.repository.entity.ProductEntity;
import profect.group1.goormdotcom.product.repository.entity.ProductImageEntity;
import profect.group1.goormdotcom.product.repository.mapper.ProductMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public UUID createProduct(
        final UUID brandId,
        final UUID categoryId,
        final String productName,
        final int price,
        final String description
    ) {
        final UUID productId = UUID.randomUUID();
        
        ProductEntity productEntity = new ProductEntity(
            productId, 
            brandId, 
            categoryId, 
            productName, 
            price, 
            description
        );
        productRepository.save(productEntity);
            
        return productId;
    }

    public Product updateProduct(
        final UUID productId,
        final UUID categoryId,
        final String productName,
        final int price,
        final String description
    ) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

        ProductEntity newProductEntity = new ProductEntity(
            productId, productEntity.getBrandId(), categoryId, productName, price, description
        );

        productRepository.save(newProductEntity);
        
        return ProductMapper.toDomain(newProductEntity, null);
    }

    public List<UUID> uploadProductImages(
        final UUID productId,
        final int imageCount
    ) {
        // 이미지 5장까지만 등록.
        long activeCount = productImageRepository.countByProductId(productId);
        if (activeCount >= 5 - imageCount) {
            throw new IllegalStateException("이미지는 최대 5장까지 업로드 가능합니다");
        }
        
        // TODO: Implement presignedURL
        // 
        
        List<ProductImageEntity> imageEntities = new ArrayList<>();
        for (int i = 0; i < imageCount; ++i) {
            final UUID imageId = UUID.randomUUID();
            final String imageObject = "product/" + productId + "/" + imageId;
            imageEntities.add(new ProductImageEntity(imageId, productId, imageObject));
        }

        productImageRepository.saveAll(imageEntities);

        return imageEntities.stream().map(ProductImageEntity::getId).toList();
    }

    public List<UUID> deleteProductImages(
        final UUID productId,
        final List<UUID> imageIds,
        final int imageCount
    ) {
        productImageRepository.deleteAllById(imageIds);
        return imageIds;
    }

    public void deleteProduct(
        final UUID productId
    ) {
        productRepository.deleteById(productId);
    }

    public Product getProduct(
        final UUID productId
    ) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

        List<ProductImageEntity> imageEntities = productImageRepository.findByProductId(productId);
        return ProductMapper.toDomain(productEntity, imageEntities);
    }

}
