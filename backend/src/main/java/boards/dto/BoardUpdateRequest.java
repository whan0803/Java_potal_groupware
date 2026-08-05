package boards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BoardUpdateRequest(
        @NotBlank(message = "게시판명은 필수입니다")
        @Size(max = 100, message = "게시판명은 100자 이하여야 합니다")
        String boardName,

        @Size(max = 255, message = "게시판 설명은 255자 이하여야 합니다")
        String boardDescription,

        @NotBlank(message = "첨부파일 허용 여부는 필수입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "첨부파일 허용 여부는 Y 또는 N이어야 합니다"
        )
        String attachmentYn,

        @NotBlank(message = "사용 여부는 필수입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "사용 여부는 Y 또는 N이어야 합니다"
        )
        String useYn,

        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId

) {
}
