package notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NoticeUpdateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        @NotNull(message = "게시 시작일은 필수입니다")
        LocalDate startDate,

        @NotNull(message = "게시 종료일은 필수입니다")
        LocalDate endDate,

        @NotBlank(message = "중요 공지 여부는 필수입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "중요 공지 여부는 Y 또는 N이어야 합니다"
        )
        String importantYn,

        @NotBlank(message = "사용 여부는 필수입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "사용 여부는 Y 또는 N이어야 합니다"
        )
        String useYn,

        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId,

        boolean admin

) {
}
