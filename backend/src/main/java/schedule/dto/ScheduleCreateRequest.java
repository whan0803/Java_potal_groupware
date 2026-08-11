package schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ScheduleCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,


        String content,


        String location,


        @NotBlank(message = "일정 타입은 필수입니다.")
        @Pattern(
                regexp = "^(PERSONAL|PUBLIC)$",
                message = "일정 타입은 PERSONAL 또는 PUBLIC이어야 합니다."
        )
        String scheduleType,


        @NotNull(message = "시작 시간은 필수입니다.")
        LocalDateTime startDatetime,


        @NotNull(message = "종료 시간은 필수입니다.")
        LocalDateTime endDatetime,


        @NotBlank(message = "종일 여부는 필수입니다.")
        @Pattern(
                regexp = "^[YN]$",
                message = "종일 여부는 Y 또는 N이어야 합니다."
        )
        String allDayYn
) {
}
