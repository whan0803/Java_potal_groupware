package code.dto;


import code.entity.CommonCode;

import java.time.LocalDateTime;


public record CommonCodeResponse(

        String codeGroupId,

        String codeGroupName,

        String description,

        String useYn,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

){


    public static CommonCodeResponse from(
            CommonCode code
    ){

        return new CommonCodeResponse(

                code.getCodeGroupId(),

                code.getCodeGroupName(),

                code.getDescription(),

                code.getUseYn(),

                code.getCreatedAt(),

                code.getUpdatedAt()

        );

    }

}