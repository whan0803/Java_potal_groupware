package dashboard.dto;

public record DashboardSummaryResponse(
        Long totalUserCount,
        Long activeUserCount,
        Long pendingApprovalCount,
        Long pendingReservationCount,
        Long inProgressTaskCount,
        Long monthlyScheduleCount,
        Long unreadMessageCount
) {

}
