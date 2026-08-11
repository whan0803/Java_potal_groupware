package document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record DocumentTemplateCreateRequest(

        @NotBlank(message = "양식 코드는 필수입니다.")
        String templateCode,


        @NotBlank(message = "양식명은 필수입니다.")
        String templateName,


        String templateDescription,


        @NotBlank(message = "양식 내용은 필수입니다.")
        String templateContent,


        @NotNull(message = "사용 여부는 필수입니다.")
        @Pattern(
                regexp = "Y|N",
                message = "사용 여부는 Y 또는 N만 가능합니다."
        )
        String useYn,


        Long createdBy

) {

}