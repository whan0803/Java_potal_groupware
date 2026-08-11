package schedule.dto;

import schedule.entity.Schedule;

import java.time.LocalDateTime;

public record ScheduleDetailResponse(
        Long scheduleId,

        Long userId,

        String userName,

        String title,

        String content,

        String location,

        String scheduleType,

        LocalDateTime startDatetime,

        LocalDateTime endDatetime,

        String allDayYn
) {

    public static ScheduleDetailResponse from(
            Schedule schedule
    ){
        return new ScheduleDetailResponse(
                schedule.getScheduleId(),

                schedule.getUser().getUserId(),

                schedule.getUser().getUserName(),

                schedule.getTitle(),

                schedule.getContent(),

                schedule.getLocation(),

                schedule.getScheduleType(),

                schedule.getStartDatetime(),

                schedule.getEndDatetime(),

                schedule.getAllDayYn()
        );
    }
}
