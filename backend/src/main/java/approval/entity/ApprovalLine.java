package approval.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "approval_lines",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_approval_lines_order",
                        columnNames = {
                                "approval_document_id",
                                "approval_order"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_line_id")
    private Long approvalLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_document_id", nullable = false)
    private ApprovalDocument approvalDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(name = "approval_order", nullable = false)
    private Integer approvalOrder;

    @Column(name = "approval_type", nullable = false, length = 30)
    private String approvalType = "APPROVAL";

    @Column(name = "approval_status", nullable = false, length = 30)
    private String approvalStatus = "WAITING";

    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (approvalStatus == null) {
            approvalStatus = "WAITING";
        }

        if (approvalType == null) {
            approvalType = "APPROVAL";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static ApprovalLine create(
            ApprovalDocument document,
            User approver,
            Integer approvalOrder,
            String approvalType,
            Long createdBy
    ) {

        ApprovalLine line = new ApprovalLine();

        line.approvalDocument = document;
        line.approver = approver;
        line.approvalOrder = approvalOrder;
        line.approvalType =
                approvalType == null
                        ? "APPROVAL"
                        : approvalType;

        line.approvalStatus = "WAITING";
        line.createdBy = createdBy;

        return line;
    }

    public void pending() {
        this.approvalStatus = "PENDING";
    }

    public void approve(
            String comment,
            Long userId
    ) {

        if (!"PENDING".equals(this.approvalStatus)) {
            throw new IllegalStateException(
                    "현재 결재 순서가 아닙니다."
            );
        }

        this.approvalStatus = "APPROVED";
        this.approvalComment = comment;
        this.processedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void reject(
            String comment,
            Long userId
    ) {

        if (!"PENDING".equals(this.approvalStatus)) {
            throw new IllegalStateException(
                    "현재 결재 순서가 아닙니다."
            );
        }

        this.approvalStatus = "REJECTED";
        this.approvalComment = comment;
        this.processedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}