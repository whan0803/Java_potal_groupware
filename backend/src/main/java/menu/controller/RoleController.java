package menu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import menu.dto.*;
import menu.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> createRole(
            @Valid @RequestBody RoleCreateRequest request
            ){
        Long roleId = roleService.createRole(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("roleId", roleId));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }
    //권한 상세
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(
        @PathVariable Long roleId
    ) {
       return ResponseEntity.ok(
               roleService.getRole(roleId)
       ) ;
    }

    //권한 수정
    @PutMapping("/{roleId}")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequest request
            ){
        roleService.updateRole(roleId, request);

        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long roleId

    ){
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}/menus")
    public ResponseEntity<List<RoleMenuResponse>> getRoleMenus(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                roleService.getRoleMenus(roleId)
        );
    }

    @PutMapping("/{roleId}/menus")
    public ResponseEntity<Void> saveRoleMenus(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleSaveRequest request
    ){
        roleService.saveRoleMenus(roleId, request);

        return ResponseEntity.noContent().build();
    }


}
