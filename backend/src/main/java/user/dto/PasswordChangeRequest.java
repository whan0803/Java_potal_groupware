package user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "기존 비밀번호를 입력해 주세요")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해 주세요")
        @Size(min = 6, message = "새 비밀번호는 6자 이상이어야 합니다")
        String nextPassword,

        @NotBlank(message = "비밀번호 확인을 입력해 주세요")
        String confirmPassword
) {
}
