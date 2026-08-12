package menu.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import menu.dto.*;
import menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    //메뉴 목록 조회
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenus() {
        List<MenuResponse> menus = menuService.getMenus();

        return ResponseEntity.ok(menus);
    }

    //메뉴 등록
    @PostMapping
    public ResponseEntity<Long> createMenu(
            @Valid @RequestBody MenuCreateRequest request
            ){
        Long menuId = menuService.createMenu(request);

        return ResponseEntity.created(URI.create("/api/menu/" + menuId))
                .body(menuId);

    }
    //메뉴 수정
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request
            ){
        menuService.updateMenu(menuId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/order")
    public ResponseEntity<Void> changeMenuOrder(
            @Valid @RequestBody MenuOrderRequest request
            ) {
    menuService.changeMenuOrder(request);
    return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{menuId}/disable")
    public ResponseEntity<Void> disable(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuDisableRequest request
            ) {
        menuService.disableMenu(
                menuId,
                request.userId()
        );

        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long menuId
    ) {
        menuService.deleteMenu(menuId);

        return ResponseEntity.noContent().build();
    }


}
