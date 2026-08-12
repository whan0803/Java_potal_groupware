package reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ReservationResource resource;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;



    @Column(name = "title", nullable = false, length = 200)
    private String title;



    @Column(name = "purpose", length = 500)
    private String purpose;



    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;



    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;



    @Column(name = "reservation_status", nullable = false, length = 30)
    private String reservationStatus = "REQUESTED";



    @Column(name = "approval_comment", length = 500)
    private String approvalComment;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;



    @Column(name = "processed_at")
    private LocalDateTime processedAt;



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

        createdAt = LocalDateTime.now();

    }



    public static Reservation create(

            ReservationResource resource,
            User requester,
            String title,
            String purpose,
            LocalDateTime start,
            LocalDateTime end

    ){

        Reservation reservation =
                new Reservation();


        reservation.resource = resource;
        reservation.requester = requester;
        reservation.title = title;
        reservation.purpose = purpose;
        reservation.startDatetime = start;
        reservation.endDatetime = end;
        reservation.createdBy = requester.getUserId();


        return reservation;

    }




    public void update(

            ReservationResource resource,
            String title,
            String purpose,
            LocalDateTime start,
            LocalDateTime end,
            Long userId

    ){

        this.resource = resource;
        this.title = title;
        this.purpose = purpose;
        this.startDatetime = start;
        this.endDatetime = end;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;

    }



    public void cancel(Long userId){

        this.reservationStatus = "CANCELED";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;

    }

    public void process(
            String status,
            User approver,
            String approvalComment
    ){

        this.reservationStatus = status;
        this.approver = approver;
        this.approvalComment = approvalComment;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = approver.getUserId();

    }
}
