package menu.service;

import lombok.RequiredArgsConstructor;
import menu.dto.MenuCreateRequest;
import menu.dto.MenuOrderRequest;
import menu.dto.MenuResponse;
import menu.dto.MenuUpdateRequest;
import menu.entity.Menu;
import menu.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import role.repository.RoleMenuRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;

    // 메뉴 목록 조회
    public List<MenuResponse> getMenus() {
        return menuRepository
                .findByParentMenuIsNullOrderBySortOrderAsc()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    //메뉴 등록
    @Transactional
    public Long createMenu(MenuCreateRequest request) {
        validateDuplicateMenu(
                request.menuName(),
                request.menuUrl(),
                null
        );

        Menu parentMenu = findParentMenu(request.parentMenuId());

        int menuLevel = calculateMenuLevel(parentMenu);

        Menu menu = Menu.create(
                request.menuName(),
                request.menuUrl(),
                parentMenu,
                menuLevel,
                request.sortOrder(),
                request.userId()
        );

        Menu savedMenu = menuRepository.save(menu);

        return savedMenu.getMenuId();
    }

    //메뉴 수정
    @Transactional
    public void updateMenu(
            Long menuId,
            MenuUpdateRequest request
    ) {
        Menu menu = findMenu(menuId);
        Menu parentMenu = findParentMenu(request.parentMenuId());

        validateParentMenu(menu, parentMenu);
        validateDuplicateMenu(
                request.menuName(),
                request.menuUrl(),
                menuId
        );

        int menuLevel = calculateMenuLevel(parentMenu);

        menu.update(
                request.menuName(),
                request.menuUrl(),
                parentMenu,
                menuLevel,
                request.sortOrder(),
                request.useYn(),
                request.userId()
        );
    }

    // 메뉴 순서 변경
    @Transactional
    public void changeMenuOrder(MenuOrderRequest request) {

        List<Menu> menus = request.menus().stream()
                .map(item -> findMenu(item.menuId()))
                .toList();

        validateSameParentAndLevel(menus);
        validateDuplicateMenuIds(request);
        validateDuplicateSortOrders(request);

        for (MenuOrderRequest.MenuOrderItem item : request.menus()) {
            Menu menu = findMenuInList(menus, item.menuId());

            menu.changeSortOrder(
                    item.sortOrder(),
                    request.userId()
            );
        }
    }

    // 메뉴 사용 중지
    @Transactional
    public void disableMenu(
            Long menuId,
            Long userId
    ) {
        Menu menu = findMenu(menuId);

        boolean hasActiveChildren =
                menuRepository.existsByParentMenuMenuIdAndUseYn(
                        menuId,
                        "Y"
                );

        if (hasActiveChildren) {
            throw new IllegalStateException(
                    "사용 중인 하위 메뉴가 있어 메뉴를 사용 중지할 수 없습니다."
            );
        }

        menu.disable(userId);
    }

    // 메뉴 실제 삭제
    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = findMenu(menuId);
        deleteMenuTree(menu);
    }

    private void deleteMenuTree(Menu menu) {
        List<Menu> children =
                menuRepository.findByParentMenuMenuIdOrderBySortOrderAsc(
                        menu.getMenuId()
                );

        for (Menu child : children) {
            deleteMenuTree(child);
        }

        roleMenuRepository.deleteByMenuMenuId(menu.getMenuId());
        menuRepository.delete(menu);
    }

    private void validateDuplicateMenu(
            String menuName,
            String menuUrl,
            Long menuId
    ) {
        String normalizedName = menuName == null ? "" : menuName.trim();
        String normalizedUrl = menuUrl == null ? "" : menuUrl.trim();

        boolean duplicateName = menuId == null
                ? menuRepository.existsByMenuName(normalizedName)
                : menuRepository.existsByMenuNameAndMenuIdNot(
                        normalizedName,
                        menuId
                );

        if (duplicateName) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 메뉴명입니다."
            );
        }

        if (normalizedUrl.isBlank()) {
            return;
        }

        boolean duplicateUrl = menuId == null
                ? menuRepository.existsByMenuUrl(normalizedUrl)
                : menuRepository.existsByMenuUrlAndMenuIdNot(
                        normalizedUrl,
                        menuId
                );

        if (duplicateUrl) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 메뉴 URL입니다."
            );
        }
    }

    // menuId로 메뉴 조회
    private Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "메뉴를 찾을 수 없습니다. menuId: " + menuId
                        )
                );
    }

    // 상위 메뉴 조회
    private Menu findParentMenu(Long parentMenuId) {

        if (parentMenuId == null) {
            return null;
        }

        Menu parentMenu = findMenu(parentMenuId);

        if (!"Y".equals(parentMenu.getUseYn())) {
            throw new IllegalStateException(
                    "사용 중지된 메뉴는 상위 메뉴로 지정할 수 없습니다."
            );
        }

        return parentMenu;
    }

    // 메뉴 계층 계산
    private int calculateMenuLevel(Menu parentMenu) {

        if (parentMenu == null) {
            return 1;
        }

        return parentMenu.getMenuLevel() + 1;
    }

    // 자기 자신 또는 자신의 하위 메뉴를 상위 메뉴로 설정하는지 검사
    private void validateParentMenu(
            Menu menu,
            Menu parentMenu
    ) {
        if (parentMenu == null) {
            return;
        }

        if (menu.getMenuId().equals(parentMenu.getMenuId())) {
            throw new IllegalArgumentException(
                    "자기 자신을 상위 메뉴로 지정할 수 없습니다."
            );
        }

        Menu currentMenu = parentMenu;

        while (currentMenu != null) {

            if (menu.getMenuId().equals(currentMenu.getMenuId())) {
                throw new IllegalArgumentException(
                        "자신의 하위 메뉴를 상위 메뉴로 지정할 수 없습니다."
                );
            }

            currentMenu = currentMenu.getParentMenu();
        }
    }

    // 동일한 상위 메뉴와 동일한 계층인지 검사
    private void validateSameParentAndLevel(List<Menu> menus) {

        if (menus.isEmpty()) {
            throw new IllegalArgumentException(
                    "순서를 변경할 메뉴가 없습니다."
            );
        }

        Menu firstMenu = menus.get(0);

        Long firstParentMenuId =
                getParentMenuId(firstMenu);

        Integer firstMenuLevel =
                firstMenu.getMenuLevel();

        boolean invalidMenuExists = menus.stream()
                .anyMatch(menu ->
                        !Objects.equals(
                                firstParentMenuId,
                                getParentMenuId(menu)
                        )
                                ||
                                !Objects.equals(
                                        firstMenuLevel,
                                        menu.getMenuLevel()
                                )
                );

        if (invalidMenuExists) {
            throw new IllegalArgumentException(
                    "메뉴 순서는 동일한 상위 메뉴와 동일한 계층 내에서만 변경할 수 있습니다."
            );
        }
    }

    // 요청에 동일한 메뉴 번호가 중복됐는지 검사
    private void validateDuplicateMenuIds(
            MenuOrderRequest request
    ) {
        long distinctMenuCount = request.menus().stream()
                .map(MenuOrderRequest.MenuOrderItem::menuId)
                .distinct()
                .count();

        if (distinctMenuCount != request.menus().size()) {
            throw new IllegalArgumentException(
                    "동일한 메뉴 번호가 중복되어 있습니다."
            );
        }
    }

    // 요청에 동일한 정렬 순서가 중복됐는지 검사
    private void validateDuplicateSortOrders(
            MenuOrderRequest request
    ) {
        long distinctOrderCount = request.menus().stream()
                .map(MenuOrderRequest.MenuOrderItem::sortOrder)
                .distinct()
                .count();

        if (distinctOrderCount != request.menus().size()) {
            throw new IllegalArgumentException(
                    "동일한 정렬 순서를 중복해서 지정할 수 없습니다."
            );
        }
    }

    // 목록에서 menuId에 해당하는 메뉴 찾기
    private Menu findMenuInList(
            List<Menu> menus,
            Long menuId
    ) {
        return menus.stream()
                .filter(menu ->
                        menu.getMenuId().equals(menuId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "순서를 변경할 메뉴를 찾을 수 없습니다."
                        )
                );
    }

    // 상위 메뉴 번호 반환
    private Long getParentMenuId(Menu menu) {

        if (menu.getParentMenu() == null) {
            return null;
        }

        return menu.getParentMenu().getMenuId();
    }
}
