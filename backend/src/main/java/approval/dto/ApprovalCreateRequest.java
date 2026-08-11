package approval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApprovalCreateRequest(
        Long templateId,

        @NotNull
        Long drafterId,

        @NotBlank
        String title,

        @NotBlank
        String content,

        List<@Valid ApprovalLineRequest> approvalLines

) {
}
