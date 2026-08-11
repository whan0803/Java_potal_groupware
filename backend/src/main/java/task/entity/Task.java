package task.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;


    // 상위 업무
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;


    // 요청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;


    // 담당자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;


    @Column(name = "title", nullable = false, length = 200)
    private String title;


    @Column(name = "content", columnDefinition = "TEXT")
    private String content;


    @Column(name = "task_status", nullable = false, length = 30)
    private String taskStatus = "READY";


    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "NORMAL";


    @Column(name = "start_date")
    private LocalDate startDate;


    @Column(name = "due_date")
    private LocalDate dueDate;


    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @Column(name = "progress_rate", nullable = false)
    private Integer progressRate = 0;


    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn = "Y";


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "created_by")
    private Long createdBy;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "updated_by")
    private Long updatedBy;



    @PrePersist
    public void createDate(){

        this.createdAt = LocalDateTime.now();

    }


    @PreUpdate
    public void updateDate(){

        this.updatedAt = LocalDateTime.now();

    }



    public static Task create(
            User requester,
            User assignee,
            String title,
            String content,
            String taskStatus,
            String priority,
            LocalDate startDate,
            LocalDate dueDate
    ){

        Task task = new Task();

        task.requester = requester;
        task.assignee = assignee;
        task.title = title;
        task.content = content;
        task.taskStatus = taskStatus;
        task.priority = priority;
        task.startDate = startDate;
        task.dueDate = dueDate;

        return task;
    }


    public void update(
            User assignee,
            String title,
            String content,
            String taskStatus,
            String priority,
            LocalDate startDate,
            LocalDate dueDate
    ){

        this.assignee = assignee;
        this.title = title;
        this.content = content;
        this.taskStatus = taskStatus;
        this.priority = priority;
        this.startDate = startDate;
        this.dueDate = dueDate;

    }


    public void changeStatus(String status){

        this.taskStatus = status;

        if(status.equals("COMPLETED")){
            this.completedAt = LocalDateTime.now();
            this.progressRate = 100;
        }

    }


    public void changeProgress(Integer progressRate){

        if(progressRate < 0 || progressRate > 100){
            throw new IllegalArgumentException(
                    "진행률은 0~100 사이여야 합니다."
            );
        }

        this.progressRate = progressRate;

    }


    public void delete(){

        this.useYn = "N";

    }
}
