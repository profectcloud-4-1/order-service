package profect.group1.goormdotcom.product.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {
    
    private UUID id;
    private UUID productId;
    private String imageObject;
    private LocalDateTime deletedAt;   

    public void updateProductId(UUID productId) {
        this.productId = productId;
    }
}
