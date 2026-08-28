package code.dto;

import java.util.List;

public record CommonCodeUpdateRequest(
        String codeGroupName,
        String description,
        String useYn,
        Long updatedBy,
        List<CommonCodeDetailRequest> details
) {
}
