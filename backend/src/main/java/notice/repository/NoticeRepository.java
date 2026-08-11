package notice.repository;

import notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

    @Query("""
            select n
            from Notice n
            where n.useYn = 'Y'
              and n.importantYn = 'Y'
              and n.startDate <= :today
              and n.endDate >= :today
            order by n.createdAt desc
            """)
    List<Notice> findImportantVisibleNotices(
            @Param("today") LocalDate today,
            Pageable pageable
    );
}
