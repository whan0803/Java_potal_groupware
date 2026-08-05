package notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "important_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String importantYn;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static Notice create(
            String title,
            String content,
            User writer,
            LocalDate startDate,
            LocalDate endDate,
            String importantYn
    ){
        Notice notice = new Notice();
        notice.title = title;
        notice.content = content;
        notice.writer = writer;
        notice.startDate = startDate;
        notice.endDate = endDate;
        notice.importantYn = importantYn;
        notice.viewCount = 0;
        notice.useYn = "Y";
        notice.createdAt = LocalDateTime.now();
        notice.createdBy = writer.getUserId();

        return notice;
    }

    public void update(
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            String importantYn,
            String useYn,
            Long userId
    ) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.importantYn = importantYn;
        this.useYn = useYn;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void increaseViewCount(){
        this.viewCount++;
    }

    public void delete(Long userId){
        this.useYn = "N";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
    public void changeImportantYn(
            String importantYn,
            Long userId
    ) {
        this.importantYn = importantYn;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public boolean isVisible(LocalDate today) {
        return "Y".equals(useYn)
                && !today.isBefore(startDate)
                && !today.isAfter(endDate);
    }


}
