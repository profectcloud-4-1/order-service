package profect.group1.goormdotcom.settings.domain;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class CommonCode {

	private String code;
	private String groupName;
	private String label;
	private String description;

	public CommonCode(
			final String code,
			final String groupName,
			final String label,
			final String description
	) {
		this.code = code;
		this.groupName = groupName;
		this.label = label;
		this.description = description;
	}

	
}
