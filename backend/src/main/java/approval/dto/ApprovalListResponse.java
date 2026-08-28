package approval.dto;

import approval.entity.ApprovalDocument;

import java.time.LocalDateTime;

public record ApprovalListResponse(

        Long approvalDocumentId,

        String documentNumber,

        Long templateId,

        String templateName,

        String title,

        Long drafterId,

        String drafterName,

        String approvalStatus,

        String approvalStatusName,

        LocalDateTime requestedAt,

        LocalDateTime completedAt,

        LocalDateTime createdAt

) {

    public static ApprovalListResponse from(
            ApprovalDocument document
    ) {
        return from(document, document.getApprovalStatus());
    }

    public static ApprovalListResponse from(
            ApprovalDocument document,
            String approvalStatusName
    ) {

        return new ApprovalListResponse(

                document.getApprovalDocumentId(),

                document.getDocumentNumber(),

                document.getTemplate() == null ? null : document.getTemplate().getTemplateId(),

                document.getTemplate() == null ? null : document.getTemplate().getTemplateName(),

                document.getTitle(),

                document.getDrafter().getUserId(),

                document.getDrafter().getUserName(),

                document.getApprovalStatus(),

                approvalStatusName,

                document.getRequestedAt(),

                document.getCompletedAt(),

                document.getCreatedAt()
        );
    }
}
