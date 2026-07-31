package user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "role_description", length = 255)
    private String roleDescription;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private long updatedBy;

    private Role(
            Long roleId,
            String roleCode,
            String roleName,
            String roleDescription,
            String useYn,
            Long createdBy
    ){
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.useYn = useYn;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public static Role create(
            String roleCode,
            String roleName,
            String roleDescription,
            String useYn,
            Long createdBy

    ){
        return new Role(
                roleCode,
                roleName,
                roleDescription,
                useYn,
                createdBy
        );
    }

    public void update(
            String roleName,
            String roleDescription,
            String useYn,
            Long updatedBy
    ){
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.useYn = useYn;
        this.updatedBy = updatedBy;
    }
}
