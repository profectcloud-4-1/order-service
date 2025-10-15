package profect.group1.goormdotcom.user.presentation.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import profect.group1.goormdotcom.user.domain.User;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {

    public static RegisterResponseDto of(User user) {
        return new RegisterResponseDto(user.getId().toString());
    }

    private String id;
}