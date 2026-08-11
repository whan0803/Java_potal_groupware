package task.dto;

import java.time.LocalDate;


public record TaskUpdateRequest(

        Long assigneeId,

        String title,

        String content,

        String taskStatus,

        String priority,

        LocalDate startDate,

        LocalDate dueDate

) {
}