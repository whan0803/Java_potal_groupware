package reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reservation.entity.ReservationResource;

import java.util.List;
import java.util.Optional;

public interface ReservationResourceRepository extends JpaRepository<ReservationResource, Long> {
    List<ReservationResource>
    findByResourceTypeAndUseYn(
            String type,
            String useYn
    );

    Optional<ReservationResource> findByResourceTypeAndResourceNameAndUseYn(
            String resourceType,
            String resourceName,
            String useYn
    );

    boolean existsByResourceTypeAndResourceNameAndUseYn(
            String resourceType,
            String resourceName,
            String useYn
    );
}
