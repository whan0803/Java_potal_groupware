package user.dto;


import user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String loginId;
    private String userName;
    private String email;
    private String phone;
    private String useYn;
    private LocalDateTime createAt;
    private List<RoleResponse> roles;

    public static UserResponse from(User user) {
        List<RoleResponse> roles = user.getUserRoles().stream()
                .map(userRole -> new RoleResponse(
                        userRole.getRole().getRoleId(),
                        userRole.getRole().getRoleCode(),
                        userRole.getRole().getRoleName()
                )).toList();

        return UserResponse.builder()
                .userId(user.getUser_id())
                .loginId(user.getLoginId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .useYn(user.getUseYn())
                .createAt(user.getCreatedAt())
                .roles(roles)
                .build();
    }

    public record  RoleResponse(
            Long roleId,
            String roleCOde,
            String roleName
    ){

    }

}
