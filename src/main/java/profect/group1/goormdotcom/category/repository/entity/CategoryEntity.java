package profect.group1.goormdotcom.category.repository.entity;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Filter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import profect.group1.goormdotcom.common.domain.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

@Entity
@Table(name = "p_category")
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
public class CategoryEntity extends BaseEntity{

    @Id
    private UUID id;
    private UUID parentId;
    private String name;
    private LocalDateTime deletedAt;

    
}
