package dashboard.dto;

import java.time.LocalDateTime;


//결제 대기
public record ApprovalDashboardResponse(
        Long approvalId,
        String title,
        String drafterName,
        LocalDateTime createAt
) {
}
