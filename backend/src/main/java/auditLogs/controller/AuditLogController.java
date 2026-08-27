package auditLogs.controller;

import auditLogs.dto.AuditLogRequest;
import auditLogs.dto.AuditLogResponse;
import auditLogs.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import security.MenuPermissionService;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final MenuPermissionService menuPermissionService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getLogs(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Long actorId,
            Pageable pageable,
            Authentication authentication
    ) {
        menuPermissionService.requireRead(
                authentication,
                "/logs",
                "/audit-logs",
                "/api/audit-logs"
        );

        return ResponseEntity.ok(
                auditLogService.getLogs(
                        tableName,
                        actionType,
                        actorId,
                        pageable
                )
        );
    }

    @PostMapping
    public ResponseEntity<Long> createLog(
            @Valid @RequestBody AuditLogRequest request
    ) {
        return ResponseEntity.ok(
                auditLogService.createLog(request)
        );
    }
}
