package dashboard.dto;

import java.util.List;

public record DashboardResponse(
        DashboardSummaryResponse summary,
        List<NoticeDashboardResponse> notices,
        List<ApprovalDashboardResponse> pendingApprovals,
        List<ReservationDashboardResponse> pendingReservations,
        List<TaskDashboardResponse> inProgressTasks,
        List<ScheduleDashboardResponse> todaySchedules,
        List<MessageDashboardResponse> receivedMessages,
        List<ChangeHistoryDashboardResponse> recentChanges
) {
}
