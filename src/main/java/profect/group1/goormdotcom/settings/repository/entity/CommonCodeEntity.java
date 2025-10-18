package profect.group1.goormdotcom.settings.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import profect.group1.goormdotcom.common.domain.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder

@Entity
@Table(name = "p_common_code")
@EntityListeners(AuditingEntityListener.class)
public class CommonCodeEntity extends BaseEntity {

	@Id
	private String code;
	@Column(name = "group_name")
	private String groupName;
	@Column(name = "label")
	private String label;
	@Column(name = "description")
	private String description;

	public CommonCodeEntity(final String code, final String groupName, final String label, final String description) {
		this.code = code;
		this.groupName = groupName;
		this.label = label;
		this.description = description;
	}
}
