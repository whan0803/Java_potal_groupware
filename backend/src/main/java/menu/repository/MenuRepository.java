package menu.repository;

import menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentMenuIsNullOrderBySortOrderAsc();

    List<Menu> findByParentMenuMenuIdOrderBySortOrderAsc(
            Long parentMenuId
    );

    List<Menu> findByUseYnOrderByMenuLevelAscSortOrderAsc(
            String useYn
    );

    boolean existsByParentMenuMenuIdAndUseYn(
            Long parentMenuId,
            String useYn
    );

    boolean existsByMenuName(String menuName);

    boolean existsByMenuUrl(String menuUrl);

    boolean existsByMenuNameAndMenuIdNot(
            String menuName,
            Long menuId
    );

    boolean existsByMenuUrlAndMenuIdNot(
            String menuUrl,
            Long menuId
    );
}
