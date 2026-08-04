package menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_menu_id")
    private Menu parentMenuId;

    @OneToMany(mappedBy = "parentMenuId")
    @OrderBy("sortOrder ASC")
    private List<Menu> childMenus = new ArrayList<>();

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_url", length = 255)
    private String menuUrl;

    @Column(name = "menu_level", nullable = false)
    private Integer menuLevel;


    @Column(name = "sort_order", nullable = false, length = 1)
    private Integer sortOrder;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;

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

    @OneToMany(mappedBy = "menu")
    private List<RoleMenu> roleMenus = new ArrayList<>();

    private Menu(
            Menu parentMenuId,
            String menuName,
            String menuUrl,
            Integer menuLevel,
            Integer sortOrder,
            String useYn,
            User createdBy
    ) {
        this.parentMenuId = parentMenuId;
        this.menuName = menuName;
        this.menuUrl = menuUrl;
        this.menuLevel = menuLevel;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public static Menu create(
            Menu parentMenuId,
            String menuName,
            String menuUrl,
            Integer menuLevel,
            Integer sortOrder,
            String useYn,
            User createdBy
    ){
        return new Menu(
                parentMenuId,
                menuName,
                menuUrl,
                menuLevel,
                sortOrder,
                useYn,
                createdBy
        );
    }

    public void update(
            Menu parentMenuId,
            String menuName,
            String menuUrl,
            Integer menuLevel,
            Integer sortOrder,
            String useYn,
            User updatedBy
    ){
        this.parentMenuId = parentMenuId;
        this.menuName = menuName;
        this.menuUrl = menuUrl;
        this.menuLevel = menuLevel;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void ChangeUseYn(String useYn, User updatedBy) {
        this.useYn = useYn;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }




}
