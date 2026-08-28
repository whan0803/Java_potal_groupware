package code.dto;

public record CommonCodeDetailRequest(
        String codeValue,
        String codeName,
        Integer sortOrder,
        String useYn
) {
}
