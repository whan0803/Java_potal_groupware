package auditLogs.dto;

import auditLogs.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResponse(

        Long logId,

        String tableName,

        Long recordId,

        String actionType,

        Map<String, Object> beforeData,

        Map<String, Object> afterData,

        Long actorId,

        LocalDateTime createdAt

) {

    public static AuditLogResponse from(AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getLogId(),
                auditLog.getTableName(),
                auditLog.getRecordId(),
                auditLog.getActionType(),
                auditLog.getBeforeData(),
                auditLog.getAfterData(),
                auditLog.getActorId(),
                auditLog.getCreatedAt()
        );
    }
}
