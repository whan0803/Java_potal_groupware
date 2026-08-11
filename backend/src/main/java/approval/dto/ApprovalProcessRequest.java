package approval.dto;

import jakarta.validation.constraints.NotNull;

public record ApprovalProcessRequest(
        @NotNull
        Long approverId,

        String comment
) {
}
