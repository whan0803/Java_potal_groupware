package user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "사용자명은 필수입니다")
    private String userName;

    @Email(message = "이메일 형식이 아닙니다")
    private String email;

    private String phone;

    private String useYn;

    private List<Long> rolesIds;
}
