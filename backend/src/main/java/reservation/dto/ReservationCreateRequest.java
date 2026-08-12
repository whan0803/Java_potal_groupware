package reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
        Long resourceId,

        String resourceName,

        String resourceType,

        @NotNull(message = "예약자는 필수입니다.")
        Long requesterId,

        @NotBlank(message = "예약 제목은 필수입니다.")
        @Size(max = 200, message = "예약 제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "예약 목적은 500자 이하여야 합니다.")
        String purpose,

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalDateTime startDateTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalDateTime endDateTime
) {

}
