package reservation.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_resources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType;

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "resource_description", length = 255)
    private String resourceDescription;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

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

        if(useYn == null) {
            useYn = "Y";
        }
    }

    public static ReservationResource create(
            String resourceType,
            String resourceName,
            String description,
            Integer capacity,
            String location,
            String vehicleNumber
    ){

        ReservationResource resource =
                new ReservationResource();

        resource.resourceType = resourceType;
        resource.resourceName = resourceName;
        resource.resourceDescription = description;
        resource.capacity = capacity;
        resource.location = location;
        resource.vehicleNumber = vehicleNumber;

        return resource;
    }

}
