package user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "사용자 명은 50자 이하여야 합니다")
    private String userName;

    @Email(message = "이메일 형식이 아닙니다")
    private String email;


    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phone;

    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다")
    private String password;

    @Pattern(
            regexp = "Y|N",
            message = "사용 여부는 Y 또는 N이어야 합니다"
    )

    private String useYn;

    private List<Long> roleIds;
}
