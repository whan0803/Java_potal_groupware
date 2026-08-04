package role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest (
        @NotBlank(message = "권한 코드는 필수입니다")
        @Size(max = 30, message = "권한 코드는 30자 이하로 입력해야 합니다")
        String roleCode,

        @NotBlank(message = "권한명은 필수입니다")
        @Size(max = 50, message = "권한명은 50자 이하로 입력해야 합니다")
        String roleName,

        @Size(max = 255, message = "권한 설명은 255자 이하로 입력해야 합니다")
        String roleDescription,

        @NotBlank(message = "사용 여부는 필수 입니다")
        @Pattern(
                regexp = "^[YN]$",
                message = "사용 여부는 Y 또는 N이어야 합니다"
        )
        String useYn,

        @NotNull(message = "등록자 번호는 필수입니다")
        Long createdBy
){}
