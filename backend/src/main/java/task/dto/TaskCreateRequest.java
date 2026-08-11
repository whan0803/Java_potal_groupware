package task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record TaskCreateRequest(

        @NotNull
        Long requesterId,

        @NotNull
        Long assigneeId,


        @NotBlank
        String title,


        String content,


        String taskStatus,


        String priority,


        LocalDate startDate,


        LocalDate dueDate

) {
}