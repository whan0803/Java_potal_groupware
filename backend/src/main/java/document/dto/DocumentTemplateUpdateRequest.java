package document.dto;


import jakarta.validation.constraints.Pattern;


public record DocumentTemplateUpdateRequest(


        String templateName,


        String templateDescription,


        String templateContent,


        @Pattern(
                regexp = "Y|N",
                message = "사용 여부는 Y 또는 N만 가능합니다."
        )
        String useYn,


        Long updatedBy


) {

}