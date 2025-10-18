package profect.group1.goormdotcom.settings.repository.mapper;

import org.springframework.stereotype.Component;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import profect.group1.goormdotcom.settings.repository.entity.CommonCodeEntity;

@Component
public class CommonCodeMapper {

	public static CommonCode toDomain(final CommonCodeEntity entity) {
		return new CommonCode(
				entity.getCode(),
				entity.getGroupName(),
				entity.getLabel(),
				entity.getDescription()
		);
	}

	public static CommonCodeEntity toEntity(final CommonCode domain) {
		return new CommonCodeEntity(
				domain.getCode(),
				domain.getGroupName(),
				domain.getLabel(),
				domain.getDescription()
		);
	}
}
