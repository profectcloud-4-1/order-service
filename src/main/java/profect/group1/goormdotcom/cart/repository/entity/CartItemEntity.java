package profect.group1.goormdotcom.cart.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

@Entity
@Table(name = "cart_item")
@SQLRestriction(value="deleted_at is NULL")
@SQLDelete(sql = "update cart_item set deleted_at = NOW(), cartId = NULL where id = ?")
@EntityListeners(AuditingEntityListener.class)
public class CartItemEntity {

	@Id
	private UUID id;
	private UUID cartId;
	private UUID productId;
	private int quantity;
	private int price;
	private int deliveryCost;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
}
