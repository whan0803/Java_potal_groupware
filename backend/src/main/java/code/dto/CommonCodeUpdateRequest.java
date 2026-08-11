package code.dto;

public record CommonCodeUpdateRequest(
        String codeGroupName,
        String description,
        String useYn,
        Long updatedBy
) {
}
