package dashboard.dto;


import java.time.LocalDateTime;
//오늘 일정
public record ScheduleDashboardResponse(
        Long scheduleId,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
