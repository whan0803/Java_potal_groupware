package dashboard.dto;

import java.time.LocalDateTime;

//공지사항
public record NoticeDashboardResponse(
        Long noticeId,
        String title,
        String writerName,
        LocalDateTime createAt
) {
}
