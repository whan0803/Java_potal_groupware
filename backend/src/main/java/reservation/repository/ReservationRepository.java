package reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import reservation.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByResourceResourceIdAndUseYnOrderByStartDatetimeAsc(
            Long resourceId,
            String useYn
    );

    @Query("""
            select r
            from Reservation r
            where r.resource.resourceId = :resourceId
              and r.useYn = 'Y'
              and r.reservationStatus <> 'CANCELED'
              and r.startDatetime < :end
              and r.endDatetime > :start
            """)
    List<Reservation> findOverlappingReservations(
            @Param("resourceId") Long resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByReservationStatusAndUseYn(
            String reservationStatus,
            String useYn
    );

    List<Reservation> findByReservationStatusAndUseYnOrderByStartDatetimeAsc(
            String reservationStatus,
            String useYn,
            Pageable pageable
    );
}
