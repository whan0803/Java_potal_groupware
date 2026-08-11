package schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name="schedules")
@Getter
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String location;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;


    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;


    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;


    @Column(name = "all_day_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String allDayYn;


    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "created_by")
    private Long createdBy;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "updated_by")
    private Long updatedBy;

    private Schedule(
        User user,
        String title,
        String content,
        String location,
        String scheduleType,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        String allDayYn
    ){
        this.user = user;
        this.title = title;
        this.content = content;
        this.location = location;
        this.scheduleType = scheduleType;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.allDayYn = allDayYn;

        this.useYn = "Y";
        this.createdAt = LocalDateTime.now();
        this.createdBy = user.getUserId();
    }

    public static Schedule create(
            User user,
            String title,
            String content,
            String location,
            String scheduleType,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String allDayYn
    ){

        return new Schedule(
                user,
                title,
                content,
                location,
                scheduleType,
                startDatetime,
                endDatetime,
                allDayYn
        );
    }

    public void update(
            String title,
            String content,
            String location,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            Long userId
    ){
        this.title = title;
        this.content = content;
        this.location = location;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void delete(Long userId) {
        this.useYn = "N";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}
