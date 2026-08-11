package approval.dto;

import approval.entity.ApprovalLine;

import java.time.LocalDateTime;

public record ApprovalLineResponse(

        Long approvalLineId,

        Long approverId,

        String approverName,

        Integer approvalOrder,

        String approvalType,

        String approvalStatus,

        String approvalComment,

        LocalDateTime processedAt

) {

    public static ApprovalLineResponse from(
            ApprovalLine line
    ) {

        return new ApprovalLineResponse(

                line.getApprovalLineId(),

                line.getApprover().getUserId(),

                line.getApprover().getUserName(),

                line.getApprovalOrder(),

                line.getApprovalType(),

                line.getApprovalStatus(),

                line.getApprovalComment(),

                line.getProcessedAt()
        );
    }
}