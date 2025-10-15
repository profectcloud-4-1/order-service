package profect.group1.goormdotcom.user.presentation.dto.response;

import profect.group1.goormdotcom.user.domain.User;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class MeResponseDto {
    public static MeResponseDto of(User user) {
        return new MeResponseDto(user);
    }

    private User me;
}