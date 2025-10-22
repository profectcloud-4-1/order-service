package profect.group1.goormdotcom.stock.repository.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import profect.group1.goormdotcom.common.domain.BaseEntity;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_stock")
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "update p_stock set deleted_at = NOw() WHERE id = ?")
public class StockEntity extends BaseEntity{
    
    @Id
    private UUID id;
    private UUID productId;
    private int stockQuantity;
    private LocalDateTime deletedAt;

    public StockEntity(
        UUID id,
        UUID productId,
        int stockQuantity
    ) {
        this.id = id;
        this.productId = productId;
        this.stockQuantity = stockQuantity;
    }

    public void updateQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

}
