package notice.dto;

import notice.entity.Notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoticeListResponse(
        Long noticeId,
        String title,
        Long writerId,
        String writerName,
        LocalDate startDate,
        LocalDate endDate,
        String importantYn,
        Integer viewCount,
        String useYn,
        LocalDateTime createdAt

) {
    public static NoticeListResponse from(Notice notice){
        return new NoticeListResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getWriter().getUserId(),
                notice.getWriter().getUserName(),
                notice.getStartDate(),
                notice.getEndDate(),
                notice.getImportantYn(),
                notice.getViewCount(),
                notice.getUseYn(),
                notice.getCreatedAt()
        );
    }
}
