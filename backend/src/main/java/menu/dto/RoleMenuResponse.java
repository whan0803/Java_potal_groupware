package menu.dto;

import menu.entity.Menu;
import menu.entity.RoleMenu;

import java.time.LocalDateTime;

public record RoleMenuResponse(
        Long menuId,
        Long parentMenuId,
        String menuName,
        String menuUrl,
        Integer menuLevel,
        Integer sortOrder,
        String readYn,
        String createYn,
        String updateYn,
        String deleteYn
) {

    public static RoleMenuResponse from(
            Menu menu,
            RoleMenu roleMenu
    ) {
        return new RoleMenuResponse(
                menu.getMenuId(),
                menu.getParentMenuId() == null
                        ? null
                        : menu.getParentMenuId().getMenuId(),
                menu.getMenuName(),
                menu.getMenuUrl(),
                menu.getMenuLevel(),
                menu.getSortOrder(),
                roleMenu == null ? "N" : roleMenu.getReadYn(),
                roleMenu == null ? "N" : roleMenu.getCreateYn(),
                roleMenu == null ? "N" : roleMenu.getUpdateYn(),
                roleMenu == null ? "N" : roleMenu.getDeleteYn()
        );
    }
}