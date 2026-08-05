package notice.dto;

import notice.entity.Notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long noticeId,
        String title,
        String content,
        Long writerId,
        String writerName,
        LocalDate startDate,
        LocalDate endDate,
        String importantYn,
        Integer viewCount,
        String useYn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getWriter().getUserId(),
                notice.getWriter().getUserName(),
                notice.getStartDate(),
                notice.getEndDate(),
                notice.getImportantYn(),
                notice.getViewCount(),
                notice.getUseYn(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
