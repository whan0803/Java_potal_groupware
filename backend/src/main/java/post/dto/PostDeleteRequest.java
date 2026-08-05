package post.dto;

import jakarta.validation.constraints.NotNull;

public record PostDeleteRequest(
        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId,
        boolean admin
) {
}
