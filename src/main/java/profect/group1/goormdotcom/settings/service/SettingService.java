package profect.group1.goormdotcom.settings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import profect.group1.goormdotcom.settings.service.CommonCodeService;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class SettingService {

	private final CommonCodeService commonCodeService;

	public CommonCode createCommonCode(final String code, final String groupName, final String label, final String description) {
		return commonCodeService.create(code, groupName, label, description);
	}

	public List<CommonCode> findCommonCodeByGroup(final String groupName) {
		return commonCodeService.findManyByGroup(groupName);
	}

	public CommonCode updateCommonCode(final String code, final String group, final String label, final String description) {
		return commonCodeService.update(code, group, label, description);
	}

}
