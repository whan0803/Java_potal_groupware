package menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MenuCreateRequest(
        @NotBlank(message = "메뉴명은 필수입니다")
        @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다")
        String menuName,

        @Size(max = 225, message = "메뉴 url은 255자 이하여야합니다")
        String menuUrl,

        Long parentMenuId,

        @NotNull(message = "정렬 순서는 필수입니다")
        @PositiveOrZero(message = "정렬 순서는 0 이상이여야 합니다")
        Integer sortOrder,

        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId
) {
}
