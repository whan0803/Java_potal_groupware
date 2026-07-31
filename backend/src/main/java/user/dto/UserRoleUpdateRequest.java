package user.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserRoleUpdateRequest {
    @NotEmpty(message = "권한을 하나 이상 선택해야 합니다")
    private List<Long> roleIds;
}
