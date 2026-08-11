package code.dto;

public record CommonCodeCreateRequest(
        String codeGroupId,
        String codeGroupName,
        String description,
        String useYn,
        Long createdBy
) {
}
