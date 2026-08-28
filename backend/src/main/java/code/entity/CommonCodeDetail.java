package code.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "common_code_details")
@IdClass(CommonCodeDetailId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCodeDetail {

    @Id
    @Column(name = "code_group_id", length = 30)
    private String codeGroupId;

    @Id
    @Column(name = "code_value", length = 50)
    private String codeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_group_id", nullable = false, insertable = false, updatable = false)
    private CommonCode commonCode;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

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

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (useYn == null) {
            useYn = "Y";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static CommonCodeDetail create(
            CommonCode commonCode,
            String codeValue,
            String codeName,
            Integer sortOrder,
            String useYn,
            Long createdBy
    ) {
        CommonCodeDetail detail = new CommonCodeDetail();
        detail.commonCode = commonCode;
        detail.codeGroupId = commonCode.getCodeGroupId();
        detail.codeValue = codeValue;
        detail.codeName = codeName;
        detail.sortOrder = sortOrder;
        detail.useYn = useYn;
        detail.createdBy = createdBy;
        return detail;
    }
}
