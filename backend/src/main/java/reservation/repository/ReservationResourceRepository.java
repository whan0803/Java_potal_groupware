package reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reservation.entity.ReservationResource;

import java.util.List;

public interface ReservationResourceRepository extends JpaRepository<ReservationResource, Long> {
    List<ReservationResource>
    findByResourceTypeAndUseYn(
            String type,
            String useYn
    );
}
