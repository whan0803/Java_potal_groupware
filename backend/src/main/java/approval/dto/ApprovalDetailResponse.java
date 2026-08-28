package approval.dto;

import approval.entity.ApprovalDocument;
import approval.entity.ApprovalLine;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalDetailResponse(

        Long approvalDocumentId,

        String documentNumber,

        Long templateId,

        String templateName,

        Long drafterId,

        String drafterName,

        String title,

        String content,

        String approvalStatus,

        String approvalStatusName,

        LocalDateTime requestedAt,

        LocalDateTime completedAt,

        LocalDateTime createdAt,

        List<ApprovalLineResponse> approvalLines

) {

    public static ApprovalDetailResponse from(
            ApprovalDocument document,
            List<ApprovalLine> lines
    ) {
        return from(document, lines, document.getApprovalStatus());
    }

    public static ApprovalDetailResponse from(
            ApprovalDocument document,
            List<ApprovalLine> lines,
            String approvalStatusName
    ) {

        Long templateId = null;
        String templateName = null;

        if (document.getTemplate() != null) {

            templateId =
                    document.getTemplate().getTemplateId();

            templateName =
                    document.getTemplate().getTemplateName();
        }

        return new ApprovalDetailResponse(

                document.getApprovalDocumentId(),

                document.getDocumentNumber(),

                templateId,

                templateName,

                document.getDrafter().getUserId(),

                document.getDrafter().getUserName(),

                document.getTitle(),

                document.getContent(),

                document.getApprovalStatus(),

                approvalStatusName,

                document.getRequestedAt(),

                document.getCompletedAt(),

                document.getCreatedAt(),

                lines.stream()
                        .map(ApprovalLineResponse::from)
                        .toList()
        );
    }
}
