package notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record NoticeImportantRequest(
        @NotBlank(message = "중요 공지 여부는 필수입니다.")
        @Pattern(
                regexp = "^[YN]$",
                message = "중요 공지 여부는 Y 또는 N이어야 합니다."
        )
        String importantYn,

        @NotNull(message = "처리자 번호는 필수입니다.")
        Long userId,

        boolean admin
) {
}
