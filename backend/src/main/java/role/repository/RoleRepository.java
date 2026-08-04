package role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import user.entity.Role;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameAndRoleIdNot(
            String roleName,
            Long roleId
    );

    List<Role> findRoleByOrderByRoleIdDesc();
}
