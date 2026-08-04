package dashboard.dto;

import java.time.LocalDateTime;
//받은 쪽지
public record MessageDashboardResponse(
        Long messageId,
        String title,
        String senderName,
        LocalDateTime receivedAt,
        boolean read
) {
}
