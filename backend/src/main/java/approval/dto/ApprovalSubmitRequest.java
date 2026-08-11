package approval.dto;

import jakarta.validation.constraints.NotNull;

public record ApprovalSubmitRequest(
        @NotNull
        Long userId
) {
}
