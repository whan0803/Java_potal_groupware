package menu.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.Role;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "role_menus",
        uniqueConstraints = {
                @UniqueConstraint(
                name = "uk_role_menus",
                columnNames = {"role_id", "menu_id"}
        )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_menu_id")
    private Long roleMenuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "read_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String readYn = "Y";

    @Column(name = "create_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String createYn = "N";

    @Column(name = "update_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String updateYn = "N";

    @Column(name = "delete_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String deleteYn = "N";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    private RoleMenu(
            Role role,
            Menu menu,
            String readYn,
            String createYn,
            String updateYn,
            String deleteYn,
            User createdBy
    ) {
        this.role = role;
        this.menu = menu;
        this.readYn = readYn;
        this.createYn = createYn;
        this.updateYn = updateYn;
        this.deleteYn = deleteYn;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public static RoleMenu create(
            Role role,
            Menu menu,
            String readYn,
            String createYn,
            String updateYn,
            String deleteYn,
            User createdBy
    ) {
        return new RoleMenu(
                role,
                menu,
                readYn,
                createYn,
                updateYn,
                deleteYn,
                createdBy
        );
    }

    public void updatePermission(
            String readYn,
            String createYn,
            String updateYn,
            String deleteYn,
            User updatedBy
    ) {
        this.readYn = readYn;
        this.createYn = createYn;
        this.updateYn = updateYn;
        this.deleteYn = deleteYn;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }


}
