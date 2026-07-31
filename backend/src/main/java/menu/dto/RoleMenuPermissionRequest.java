package menu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RoleMenuPermissionRequest(

        @NotNull(message = "메뉴 번호는 필수 입니다")
        Long menuId,

        @NotNull(message = "조호; 권한은 필수입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "조회 권한은 Y 또는 N이어야 합니다."
        )
        String readYn,
        @NotNull(message = "등록 권한은 필수입니다.")
        @Pattern(
                regexp = "^[YN]$",
                message = "등록 권한은 Y 또는 N이어야 합니다."
        )
        String createYn,

        @NotNull(message = "수정 권한은 필수입니다.")
        @Pattern(
                regexp = "^[YN]$",
                message = "수정 권한은 Y 또는 N이어야 합니다."
        )
        String updateYn,

        @NotNull(message = "삭제 권한은 필수입니다.")
        @Pattern(
                regexp = "^[YN]$",
                message = "삭제 권한은 Y 또는 N이어야 합니다."
        )
        String deleteYn
) {
}
