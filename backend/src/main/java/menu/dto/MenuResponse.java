package menu.dto;

import menu.entity.Menu;

import java.util.List;

public record MenuResponse(
        Long menuId,
        String menuName,
        String menuUrl,
        Long parentMenuId,
        String parentMenuName,
        Integer menuLevel,
        Integer sortOrder,
        String useYn,
        List<MenuResponse> children
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getMenuId(),
                menu.getMenuName(),
                menu.getMenuUrl(),
                menu.getParentMenu() == null
                        ? null
                        : menu.getParentMenu().getMenuId(),
                menu.getParentMenu() == null
                        ? null
                        : menu.getParentMenu().getMenuName(),
                menu.getMenuLevel(),
                menu.getSortOrder(),
                menu.getUseYn(),
                menu.getChildMenus().stream()
                        .map(MenuResponse::from)
                        .toList()
        );
    }
}