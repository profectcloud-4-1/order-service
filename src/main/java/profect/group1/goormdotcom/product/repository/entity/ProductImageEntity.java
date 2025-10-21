package profect.group1.goormdotcom.product.repository.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import profect.group1.goormdotcom.common.domain.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

@Entity
@Table(name = "p_product_image")
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "update product set deleted_at = NOW() where id = ?")
@EntityListeners(AuditingEntityListener.class)
public class ProductImageEntity extends BaseEntity{
    @Id
    private UUID id;
    private UUID productId;
    private String imageObject;
    private LocalDateTime createAt;
    private LocalDateTime deletedAt;

    public ProductImageEntity(
        final UUID id,
        final UUID productId,
        final String imageObject
    ) {
        this.id = id;
        this.productId = productId;
        this.imageObject = imageObject;
    }
}
