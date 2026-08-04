package auth.dto;

import java.util.List;

public record CurrentUserResponse(
        Long userId,
        String loginId,
        String userName,
        List<String> roles
) {
}
