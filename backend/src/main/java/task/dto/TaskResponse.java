package task.dto;

import task.entity.Task;

import java.time.LocalDate;


public record TaskResponse(

        Long taskId,

        String title,

        String content,

        String taskStatus,

        String priority,

        LocalDate startDate,

        LocalDate dueDate,

        Integer progressRate,

        Long requesterId,

        String requesterName,

        Long assigneeId,

        String assigneeName

) {


    public static TaskResponse from(Task task){

        return new TaskResponse(

                task.getTaskId(),

                task.getTitle(),

                task.getContent(),

                task.getTaskStatus(),

                task.getPriority(),

                task.getStartDate(),

                task.getDueDate(),

                task.getProgressRate(),

                task.getRequester().getUserId(),

                task.getRequester().getUserName(),

                task.getAssignee().getUserId(),

                task.getAssignee().getUserName()

        );

    }

}