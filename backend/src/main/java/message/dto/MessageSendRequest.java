package message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(

        @NotNull(message = "수신자는 필수입니다")
        Long receiveId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(
                max = 200,
                message = "제목은 200자 이하여야 합니다"
        )
        String title,

        @NotBlank (message = "내용은 필수입니다")
        String content
) {
}
