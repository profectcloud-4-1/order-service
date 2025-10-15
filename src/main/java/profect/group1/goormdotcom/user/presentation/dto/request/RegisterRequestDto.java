package profect.group1.goormdotcom.user.presentation.dto.request;
import profect.group1.goormdotcom.user.domain.enums.UserRole;
import lombok.ToString;
import lombok.Getter;
import lombok.AllArgsConstructor;

@ToString
@Getter
@AllArgsConstructor
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private String brandId;
}