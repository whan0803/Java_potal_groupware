package approval.entity;

import document.entity.DocumentTemplate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_document_id")
    private Long approvalDocumentId;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private DocumentTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drafter_id", nullable = false)
    private User drafter;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "approval_status", nullable = false, length = 30)
    private String approvalStatus = "DRAFT";

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (approvalStatus == null) {
            approvalStatus = "DRAFT";
        }

        if (useYn == null) {
            useYn = "Y";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static ApprovalDocument create(
            String documentNumber,
            DocumentTemplate template,
            User drafter,
            String title,
            String content
    ) {

        ApprovalDocument document = new ApprovalDocument();

        document.documentNumber = documentNumber;
        document.template = template;
        document.drafter = drafter;
        document.title = title;
        document.content = content;
        document.approvalStatus = "DRAFT";
        document.createdBy = drafter.getUserId();

        return document;
    }

    public void updateDraft(
            DocumentTemplate template,
            String title,
            String content,
            Long userId
    ) {

        if (!"DRAFT".equals(this.approvalStatus)) {
            throw new IllegalStateException(
                    "임시저장 상태의 문서만 수정할 수 있습니다."
            );
        }

        this.template = template;
        this.title = title;
        this.content = content;
        this.updatedBy = userId;
    }

    public void submit() {

        if (!"DRAFT".equals(this.approvalStatus)) {
            throw new IllegalStateException(
                    "임시저장 문서만 상신할 수 있습니다."
            );
        }

        this.approvalStatus = "IN_PROGRESS";
        this.requestedAt = LocalDateTime.now();
    }

    public void approveComplete() {

        this.approvalStatus = "APPROVED";
        this.completedAt = LocalDateTime.now();
    }

    public void reject() {

        this.approvalStatus = "REJECTED";
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {

        if ("APPROVED".equals(this.approvalStatus)
                || "REJECTED".equals(this.approvalStatus)
                || "CANCELED".equals(this.approvalStatus)) {

            throw new IllegalStateException(
                    "취소할 수 없는 문서입니다."
            );
        }

        this.approvalStatus = "CANCELED";
        this.completedAt = LocalDateTime.now();
    }
}
