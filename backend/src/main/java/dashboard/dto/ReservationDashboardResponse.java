package dashboard.dto;

import java.time.LocalDateTime;
//에약 승인 대기
public record ReservationDashboardResponse(
        Long reservationId,
        String resourceName,
        String requesterName,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
