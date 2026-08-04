package dashboard.dto;

import java.time.LocalDate;

//진행 중 업무
public record TaskDashboardResponse(
        Long taskId,
        String title,
        Integer progressRate,
        String managerName,
        LocalDate dueDate
) {
}
