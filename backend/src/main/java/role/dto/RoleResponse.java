package role.dto;

import user.entity.Role;

import java.time.LocalDateTime;

public record RoleResponse(
        Long roleId,
        String roleCode,
        String roleName,
        String roleDescription,
        String useYn,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy

) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getRoleId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getRoleDescription(),
                role.getUseYn(),
                role.getCreatedAt(),
                role.getCreatedBy(),
                role.getUpdatedAt(),
                role.getUpdatedBy()
        );

    }
}
