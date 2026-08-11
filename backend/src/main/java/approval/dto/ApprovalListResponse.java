package approval.dto;

import approval.entity.ApprovalDocument;

import java.time.LocalDateTime;

public record ApprovalListResponse(

        Long approvalDocumentId,

        String documentNumber,

        String title,

        Long drafterId,

        String drafterName,

        String approvalStatus,

        LocalDateTime requestedAt,

        LocalDateTime completedAt,

        LocalDateTime createdAt

) {

    public static ApprovalListResponse from(
            ApprovalDocument document
    ) {

        return new ApprovalListResponse(

                document.getApprovalDocumentId(),

                document.getDocumentNumber(),

                document.getTitle(),

                document.getDrafter().getUserId(),

                document.getDrafter().getUserName(),

                document.getApprovalStatus(),

                document.getRequestedAt(),

                document.getCompletedAt(),

                document.getCreatedAt()
        );
    }
}