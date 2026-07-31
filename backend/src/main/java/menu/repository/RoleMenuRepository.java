package menu.repository;

import menu.entity.RoleMenu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {
    @EntityGraph(attributePaths = {"role", "menu"})
    List<RoleMenu> findByRoleRoleId(Long roleId);

    Optional<RoleMenu> findByRoleRoleIdAndMenuMenuId(
            Long roleId,
            Long menuId
    );

    boolean existsByRoleRoleIdAndMenuMenuId(
            Long roleId,
            Long menuId
    );

    void deleteByRoleRoleId(Long longId);

    boolean existsByRoleRoleId(Long roleId);
}
