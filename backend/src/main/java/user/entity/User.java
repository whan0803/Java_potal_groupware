package user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length=255)
    private String password;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

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

    @OneToMany(
            mappedBy =  "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    private  List<UserRole> userRoles = new ArrayList<>();

    public User(
            String loginId,
            String password,
            String userName,
            String email,
            String phone,
            String useYn,
            Long createdBy
    ) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.useYn = useYn == null ? "Y" : useYn;
        this.createdBy = createdBy;
    }

    public void update(
            String userName,
            String email,
            String phone,
            String useYn,
            Long updatedBy
    ) {
        if (userName != null) {
            this.userName = userName;
        }

        this.email = email;
        this.phone = phone;

        if (useYn != null) {
            this.useYn = useYn;
        }

        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(Long updatedBy) {
        this.useYn = "N";
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword, Long updatedBy) {
        this.password = encodedPassword;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if(updatedAt == null) {
            updatedAt = createdAt;
        }

        if(useYn == null) {
            useYn = "Y";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
