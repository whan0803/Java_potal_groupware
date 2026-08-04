package role.repository;

import role.entity.Menu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @EntityGraph(attributePaths = "parentMenuId")
    List<Menu> findByUseYnOrderByMenuLevelAscSortOrderAsc(
            String useYn
    );
}
