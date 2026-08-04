package auth.dto;

import java.util.List;

public record LoginResponse(
        Long userId,
        String loginId,
        String userName,
        List<String> roles,
        String message
) {

}
