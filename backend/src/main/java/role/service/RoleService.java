package role.service;

import lombok.RequiredArgsConstructor;
import menu.dto.*;
import menu.entity.Menu;
import menu.repository.MenuRepository;
import role.dto.*;
import role.entity.RoleMenu;
import role.repository.RoleMenuRepository;
import role.repository.RoleRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.entity.Role;
import user.entity.User;
import user.entity.UserRole;
import user.repository.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;



    //권한 등록
    @Transactional
    public Long createRole(RoleCreateRequest request){
        validationYn(request.useYn(), "사용여부");

        String roleCode = request.roleCode().trim().toUpperCase();

        if(roleRepository.existsByRoleCode(roleCode)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 권한 코드입니다"
            );
        }

        if(roleRepository.existsByRoleName(request.roleName().trim())) {
            throw new IllegalArgumentException(
                    "이미 사용중인 권한명입니다"
            );
        }

        Role role = Role.create(
                roleCode,
                request.roleName().trim(),
                trimToNull(request.roleDescription()),
                request.useYn(),
                request.createdBy()

        );

        return roleRepository.save(role).getRoleId();
    }

    //권한 목록 조회
    public List<RoleResponse> getRoles(
            String roleCode,
            String roleName,
            String useYn
    ) {
        Specification<Role> specification =
                (root, query, cb) -> cb.conjunction();

        if (roleCode != null && !roleCode.isBlank()) {
            String keyword = roleCode.trim().toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("roleCode")), "%" + keyword + "%")
            );
        }

        if (roleName != null && !roleName.isBlank()) {
            String keyword = roleName.trim().toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("roleName")), "%" + keyword + "%")
            );
        }

        if (useYn != null && !useYn.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.upper(root.get("useYn")), useYn.trim().toUpperCase())
            );
        }

        return roleRepository.findAll(specification).stream()
                .sorted(Comparator.comparing(Role::getRoleId).reversed())
                .map(RoleResponse::from)
                .toList();
    }

    //권한 상세 조회
    public RoleResponse getRole(Long roleId) {
        Role role = findRole(roleId);
        return RoleResponse.from(role);
    }

    private Role findRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "권한을 찾을 수 없습니다."
                        )
                );
    }

    //권한 수정
    @Transactional
    public void updateRole(
            Long roleId,
            RoleUpdateRequest request
    ) {
        validationYn(request.useYn(), "사용 여부");

        Role role = findRole(roleId);

        String roleName = request.roleName().trim();
        if(roleRepository.existsByRoleNameAndRoleIdNot(
                roleName,
                roleId
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 권한명입니다"
            );
        }

        role.update(
                roleName,
                trimToNull(request.roleDescription()),
                request.useYn(),
                request.updatedBy()
        );
    }

    //권한 삭제
    @Transactional
    public void deleteRole(Long roleId){
        Role role = findRole(roleId);

        roleMenuRepository.deleteByRoleRoleId(roleId);

        roleRepository.delete(role);
    }

    @Transactional
    public void deactivateRole(Long roleId, Long updatedBy) {
        Role role = findRole(roleId);

        if ("N".equals(role.getUseYn())) {
            throw new IllegalStateException(
                    "이미 사용 중지된 권한입니다."
            );
        }

        role.deactivate(updatedBy);
    }


    //권힌별 메누 설정 조회
    public List<RoleMenuResponse> getRoleMenus(Long roleId) {
        findRole(roleId);
        List<Menu> menus =
                menuRepository.findByUseYnOrderByMenuLevelAscSortOrderAsc("Y");

        List<RoleMenu> roleMenus =
                roleMenuRepository.findByRoleRoleId(roleId);

        Map<Long, RoleMenu> roleMenuMap = roleMenus.stream()
                .collect(Collectors.toMap(roleMenu -> roleMenu.getMenu().getMenuId(),
                        Function.identity()));

        return menus.stream()
                .map(menu -> RoleMenuResponse.from(
                        menu,
                        roleMenuMap.get(menu.getMenuId())
                ))
                .toList();
    }

    public List<RoleMenuResponse> getMyRoleMenus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다. userId=" + userId
                        )
                );

        Set<Long> roleIds = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(role -> "Y".equals(role.getUseYn()))
                .map(Role::getRoleId)
                .collect(Collectors.toSet());

        List<Menu> menus =
                menuRepository.findByUseYnOrderByMenuLevelAscSortOrderAsc("Y");

        if (roleIds.isEmpty()) {
            return menus.stream()
                    .map(menu -> RoleMenuResponse.from(menu, null))
                    .toList();
        }

        Map<Long, PermissionSummary> permissionMap = new HashMap<>();
        roleMenuRepository.findByRoleRoleIdIn(roleIds)
                .forEach(roleMenu -> permissionMap
                        .computeIfAbsent(
                                roleMenu.getMenu().getMenuId(),
                                menuId -> new PermissionSummary()
                        )
                        .merge(roleMenu));

        return menus.stream()
                .map(menu -> {
                    PermissionSummary permission =
                            permissionMap.getOrDefault(
                                    menu.getMenuId(),
                                    new PermissionSummary()
                            );

                    return new RoleMenuResponse(
                            menu.getMenuId(),
                            menu.getParentMenu() == null
                                    ? null
                                    : menu.getParentMenu().getMenuId(),
                            menu.getMenuName(),
                            menu.getMenuUrl(),
                            menu.getMenuLevel(),
                            menu.getSortOrder(),
                            permission.readYn,
                            permission.createYn,
                            permission.updateYn,
                            permission.deleteYn
                    );
                })
                .toList();
    }

    //권한별 메뉴 crud권한 전체 저장
    @Transactional
    public void saveRoleMenus(
            Long roleId,
            RoleSaveRequest request,
            Long actorId
    ) {
        Role role = findRole(roleId);

        validateDuplicateMenuIds(request.menus());

        User createdBy = userRepository.findById(actorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "처리자 정보를 찾을 수 없습니다. userId="
                                        + actorId
                        )
                );

        List<RoleMenu> roleMenus = new ArrayList<>();

        for (RoleMenuPermissionRequest permission : request.menus()) {
            validatePermission(permission);

            if (isAllPermissionN(permission)) {
                continue;
            }

            Menu menu = menuRepository.findById(permission.menuId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "메뉴를 찾을 수 없습니다. menuId="
                                            + permission.menuId()
                            )
                    );

            if (!"Y".equals(menu.getUseYn())) {
                throw new IllegalArgumentException(
                        "사용하지 않는 메뉴에는 권한을 설정할 수 없습니다. menuId="
                                + permission.menuId()
                );
            }

            RoleMenu roleMenu = RoleMenu.create(
                    role,
                    menu,
                    permission.readYn(),
                    permission.createYn(),
                    permission.updateYn(),
                    permission.deleteYn(),
                    createdBy
            );

            roleMenus.add(roleMenu);
        }

        roleMenuRepository.deleteByRoleRoleId(roleId);
        roleMenuRepository.flush();
        roleMenuRepository.saveAll(roleMenus);
    }

    private void validatePermission(
            RoleMenuPermissionRequest permission
    ){
        validationYn(permission.readYn(), "조회 권한");
        validationYn(permission.createYn(), "등록 권한");
        validationYn(permission.updateYn(), "수정 권한");
        validationYn(permission.deleteYn(), "삭제 권한");

        if ("N".equals(permission.readYn())
                && (
                "Y".equals(permission.createYn())
                        || "Y".equals(permission.updateYn())
                        || "Y".equals(permission.deleteYn())
        )) {
            throw new IllegalArgumentException(
                    "등록, 수정, 삭제 권한이 있으면 조회 권한도 Y여야 합니다."
            );
        }
    }


    private void validationYn(
            String value,
            String fieldName
    ){
        if(!"Y".equals(value) && !"N".equals(value)) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) Y 또는 N이어야 합니다."
            );
        }
    }

    private void validateDuplicateMenuIds(
            List<RoleMenuPermissionRequest> menus
    ){
        Set<Long> menuIds = new HashSet<>();

        for(RoleMenuPermissionRequest menu : menus) {
            if(!menuIds.add(menu.menuId())) {
                throw new IllegalArgumentException(
                        "중복된 메뉴 번호가 존재합니다. menuId=" + menu.menuId()
                );
            }
        }
    }

    private boolean isAllPermissionN(
            RoleMenuPermissionRequest permission
    ) {
        return "N".equals(permission.readYn())
                && "N".equals(permission.createYn())
                && "N".equals(permission.updateYn())
                && "N".equals(permission.deleteYn());

    }

    private String trimToNull(String value) {
        if(value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class PermissionSummary {
        private String readYn = "N";
        private String createYn = "N";
        private String updateYn = "N";
        private String deleteYn = "N";

        private void merge(RoleMenu roleMenu) {
            readYn = mergeYn(readYn, roleMenu.getReadYn());
            createYn = mergeYn(createYn, roleMenu.getCreateYn());
            updateYn = mergeYn(updateYn, roleMenu.getUpdateYn());
            deleteYn = mergeYn(deleteYn, roleMenu.getDeleteYn());
        }

        private String mergeYn(String current, String next) {
            return "Y".equals(current) || "Y".equals(next) ? "Y" : "N";
        }
    }
}
