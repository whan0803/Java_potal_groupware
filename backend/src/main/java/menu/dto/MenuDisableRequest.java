package menu.dto;

import jakarta.validation.constraints.NotNull;

public record MenuDisableRequest(
        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId
) {
}
