package approval.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ApprovalLineRequest(
        @NotNull
        Long approverId,

        @NotNull
        @Min(1)
        Integer approvalOrder,

        String approvalType

) {
}
