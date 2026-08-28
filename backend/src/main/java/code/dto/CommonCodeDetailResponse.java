package code.dto;

import code.entity.CommonCodeDetail;

public record CommonCodeDetailResponse(
        String codeGroupId,
        String codeValue,
        String codeName,
        Integer sortOrder,
        String useYn
) {

    public static CommonCodeDetailResponse from(CommonCodeDetail detail) {
        return new CommonCodeDetailResponse(
                detail.getCodeGroupId(),
                detail.getCodeValue(),
                detail.getCodeName(),
                detail.getSortOrder(),
                detail.getUseYn()
        );
    }
}
