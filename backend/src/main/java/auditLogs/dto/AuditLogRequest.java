package auditLogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AuditLogRequest(

        @NotBlank(message = "테이블명은 필수입니다.")
        String tableName,

        Long recordId,

        @NotBlank(message = "작업 유형은 필수입니다.")
        String actionType,

        Map<String, Object> beforeData,

        Map<String, Object> afterData,

        @NotNull(message = "작업자 ID는 필수입니다.")
        Long actorId

) {
}
