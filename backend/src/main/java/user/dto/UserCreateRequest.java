package user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "로그인 아이디는 필수 입니다")
    @Size(min = 4, max = 20)
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 30)
    private String password;

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(max = 50)
    private String userName;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phone;


    @Pattern(
            regexp = "Y|N",
            message = "사용 여부는 Y 또는 N이어야 합니다"
    )
    private String useYn;

    private List<Long> roleIds;

}
