package menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    private Menu parentMenu;

    @OneToMany(mappedBy = "parentMenu")
    @OrderBy("sortOrder ASC")
    private List<Menu> childMenus = new ArrayList<>();

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_url", length = 255)
    private String menuUrl;

    @Column(name = "menu_level", nullable = false)
    private Integer menuLevel;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static Menu create(
            String menuName,
            String menuUrl,
            Menu parentMenu,
            Integer menuLevel,
            Integer sortOrder,
            Long userId
    ) {
        Menu menu = new Menu();

        menu.menuName = menuName;
        menu.menuUrl = menuUrl;
        menu.parentMenu = parentMenu;
        menu.menuLevel = menuLevel;
        menu.sortOrder = sortOrder;
        menu.useYn = "Y";
        menu.createdAt = LocalDateTime.now();
        menu.createdBy = userId;

        return menu;
    }

    public void update(
            String menuName,
            String menuUrl,
            Menu parentMenu,
            Integer menuLevel,
            Integer sortOrder,
            String useYn,
            Long userId
    ) {
        this.menuName = menuName;
        this.menuUrl = menuUrl;
        this.parentMenu = parentMenu;
        this.menuLevel = menuLevel;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void changeSortOrder(
            Integer sortOrder,
            Long userId
    ) {
        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void disable(Long userId) {
        this.useYn = "N";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}
