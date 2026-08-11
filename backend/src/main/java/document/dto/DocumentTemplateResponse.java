package document.dto;


import document.entity.DocumentTemplate;

import java.time.LocalDateTime;


public record DocumentTemplateResponse(


        Long templateId,

        String templateCode,

        String templateName,

        String templateDescription,

        String templateContent,

        String useYn,

        LocalDateTime createdAt,

        Long createdBy,

        LocalDateTime updatedAt,

        Long updatedBy


){


    public static DocumentTemplateResponse from(
            DocumentTemplate template
    ){

        return new DocumentTemplateResponse(

                template.getTemplateId(),

                template.getTemplateCode(),

                template.getTemplateName(),

                template.getTemplateDescription(),

                template.getTemplateContent(),

                template.getUseYn(),

                template.getCreatedAt(),

                template.getCreatedBy(),

                template.getUpdatedAt(),

                template.getUpdatedBy()

        );

    }

}