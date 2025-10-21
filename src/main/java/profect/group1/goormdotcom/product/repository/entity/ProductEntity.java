package profect.group1.goormdotcom.product.repository.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
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
@Table(name = "p_product")
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "update p_product set deleted_at = NOW() where id = ?")
@EntityListeners(AuditingEntityListener.class)
public class ProductEntity extends BaseEntity{
    @Id
    private UUID id;
    private UUID brandId;
    private UUID categoryId;
    private String name;
    private int price;
    private String description;
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;


    public ProductEntity(
        final UUID id,
        final UUID brandId,
        final UUID categoryId,
        final String name,
        final int price,
        final String description
    ) {
        this.id = id;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.description = description;
    }
}
