package schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 기간별 조회
    @Query("""
            select s
            from Schedule s
            where s.useYn = 'Y'
              and s.startDatetime <= :end
              and s.endDatetime >= :start
              and (
                    s.scheduleType = 'PUBLIC'
                    or s.user.userId = :userId
                  )
            order by s.startDatetime asc
            """)
    List<Schedule> findVisibleSchedules(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("userId") Long userId
    );

}
