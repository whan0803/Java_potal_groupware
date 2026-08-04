package menu.dto;

import jakarta.validation.constraints.*;

public record MenuUpdateRequest(
        @NotBlank(message = "메뉴명은 필수입니다")
        @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다")
        String menuName,

        @Size(max = 255, message = "메뉴 url은 255자 이하여야 합니다")
        String menuUrl,

        Long parentMenuId,

        @NotNull(message = "정렬 순서는 필수입니다")
        @PositiveOrZero(message = "정렬순서는 0 이상이어야 합니다")
        Integer sortOrder,

        @Pattern(
                regexp = "^[YN]$",
                message = "사용 여부는 Y 또는 N이어야 합니다."
        )
        String useYn,

        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId
) {
}
