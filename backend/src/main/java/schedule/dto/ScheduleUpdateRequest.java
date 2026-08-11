package schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ScheduleUpdateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        String content,

        String location,

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalDateTime startDatetime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalDateTime endDatetime
) {
}
