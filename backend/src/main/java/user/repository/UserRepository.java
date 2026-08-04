package user.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    long countByUseYn(String useYn);

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role"
    })

    Optional<User> findDetailByUserId(Long userId);

}
