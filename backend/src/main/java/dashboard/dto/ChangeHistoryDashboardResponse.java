package dashboard.dto;

import java.time.LocalDateTime;

//변경 이력
public record ChangeHistoryDashboardResponse(
        Long historyId,
        String actionType,
        String targetName,
        String description,
        String actorName,
        LocalDateTime createAt
) {
}
