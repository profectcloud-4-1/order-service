package profect.group1.goormdotcom.user.presentation.dto.response;

import profect.group1.goormdotcom.user.domain.User;
import java.util.List;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ListResponseDto {
    public static ListResponseDto of(List<User> list) {
        return new ListResponseDto(list);
    }

    private List<User> list;
}