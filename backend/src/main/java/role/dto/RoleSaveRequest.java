package role.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoleSaveRequest(
        @NotEmpty(message = "메뉴 권한 설정은 한개 이상이어야 합니다")
        List<@Valid RoleMenuPermissionRequest> menus,

        @NotNull(message = "처리자 번호는 필수 입니다")
        Long userId
) {
}
