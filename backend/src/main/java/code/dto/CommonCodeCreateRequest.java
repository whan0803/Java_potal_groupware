package code.dto;

import java.util.List;

public record CommonCodeCreateRequest(
        String codeGroupId,
        String codeGroupName,
        String description,
        String useYn,
        Long createdBy,
        List<CommonCodeDetailRequest> details
) {
}
