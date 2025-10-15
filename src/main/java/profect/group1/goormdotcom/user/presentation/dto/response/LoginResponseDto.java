package profect.group1.goormdotcom.user.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    public static LoginResponseDto of(String token) { return new LoginResponseDto(token); }
}


