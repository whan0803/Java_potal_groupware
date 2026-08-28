package code.dto;


import code.entity.CommonCode;

import java.time.LocalDateTime;
import java.util.List;


public record CommonCodeResponse(

        String codeGroupId,

        String codeGroupName,

        String description,

        String useYn,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long detailCount,

        List<CommonCodeDetailResponse> details

){


    public static CommonCodeResponse from(CommonCode code){

        return from(code, 0L, List.of());

    }


    public static CommonCodeResponse from(
            CommonCode code,
            Long detailCount,
            List<CommonCodeDetailResponse> details
    ){

        return new CommonCodeResponse(

                code.getCodeGroupId(),

                code.getCodeGroupName(),

                code.getDescription(),

                code.getUseYn(),

                code.getCreatedAt(),

                code.getUpdatedAt(),

                detailCount,

                details

        );

    }

}
