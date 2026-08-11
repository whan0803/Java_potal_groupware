package schedule.dto;

import schedule.entity.Schedule;

import java.time.LocalDateTime;

public record ScheduleResponse(
        Long scheduleId,
        String title,
        String scheduleType,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime
) {
    public static  ScheduleResponse from(
      Schedule schedule
    ){
        return new ScheduleResponse(
                schedule.getScheduleId(),
                schedule.getTitle(),
                schedule.getScheduleType(),
                schedule.getStartDatetime(),
                schedule.getEndDatetime()
        );
    }
}
