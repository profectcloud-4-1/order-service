package profect.group1.goormdotcom.settings.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import profect.group1.goormdotcom.settings.repository.CommonCodeRepository;
import profect.group1.goormdotcom.settings.repository.mapper.CommonCodeMapper;
import profect.group1.goormdotcom.settings.domain.CommonCode;
import profect.group1.goormdotcom.settings.repository.entity.CommonCodeEntity;
import profect.group1.goormdotcom.apiPayload.exceptions.handler.SettingsHandler;
import profect.group1.goormdotcom.apiPayload.code.status.ErrorStatus;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class CommonCodeService {
    private final CommonCodeRepository repo;

    // FIXME: 허용할 그룹이름 추가 시 이부분 수정.
    private static final Set<String> ALLOWED_GROUP_NAMES = Set.of(
        "DELIVERY", "ORDER", "PAYMENT", "PRODUCT", "REVIEW", "USER", "COMMON", "SETTING", "OTHER"
    );

    public CommonCodeService(CommonCodeRepository repo) {
        this.repo = repo;
    }

    public List<CommonCode> findManyByGroup(String groupName) {
        List<CommonCodeEntity> entities = repo.findAllByGroupName(groupName);
        return entities.stream()
                .map(CommonCodeMapper::toDomain)
                .collect(Collectors.toList());
    }

    public CommonCode create(String code, String groupName, String label, String description) {
        if (repo.existsById(code)) throw new SettingsHandler(ErrorStatus.COMMON_CODE_ALREADY_EXISTS);

        if (!ALLOWED_GROUP_NAMES.contains(groupName)) throw new SettingsHandler(ErrorStatus.COMMON_CODE_INVALID_GROUP_NAME);

        CommonCode commonCode = new CommonCode(code, groupName, label, description);
        return CommonCodeMapper.toDomain(repo.save(CommonCodeMapper.toEntity(commonCode)));
    }

    public CommonCode update(String code, String groupName, String label, String description) {
        CommonCodeEntity existing = repo.findById(code)
                .orElseThrow(() -> new SettingsHandler(ErrorStatus.COMMON_CODE_NOT_FOUND));

        if (groupName != null && !ALLOWED_GROUP_NAMES.contains(groupName)) throw new SettingsHandler(ErrorStatus.COMMON_CODE_INVALID_GROUP_NAME);

        if (groupName != null) existing.setGroupName(groupName);
        if (label != null) existing.setLabel(label);
        if (description != null) existing.setDescription(description);
        return CommonCodeMapper.toDomain(repo.save(existing));
    }

}