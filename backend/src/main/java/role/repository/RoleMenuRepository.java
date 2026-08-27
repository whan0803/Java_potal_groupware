package role.repository;

import role.entity.RoleMenu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {
    @EntityGraph(attributePaths = {"role", "menu"})
    List<RoleMenu> findByRoleRoleId(Long roleId);

    @EntityGraph(attributePaths = {"role", "menu"})
    List<RoleMenu> findByRoleRoleIdIn(Set<Long> roleIds);

    Optional<RoleMenu> findByRoleRoleIdAndMenuMenuId(
            Long roleId,
            Long menuId
    );

    boolean existsByRoleRoleIdAndMenuMenuId(
            Long roleId,
            Long menuId
    );

    void deleteByRoleRoleId(Long longId);

    void deleteByMenuMenuId(Long menuId);

    boolean existsByRoleRoleId(Long roleId);

    @Query("""
            select count(rm) > 0
            from RoleMenu rm
            where rm.role.useYn = 'Y'
              and rm.menu.useYn = 'Y'
              and upper(rm.role.roleCode) in :roleCodes
              and (
                    rm.menu.menuUrl in :menuUrls
                    or function('regexp_replace', rm.menu.menuUrl, '^https?://[^/]+', '') in :menuUrls
              )
              and rm.readYn = 'Y'
            """)
    boolean existsReadableMenu(
            @Param("roleCodes") Set<String> roleCodes,
            @Param("menuUrls") Set<String> menuUrls
    );
}
