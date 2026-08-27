package auditLogs.service;


import auditLogs.dto.AuditLogRequest;
import auditLogs.dto.AuditLogResponse;
import auditLogs.entity.AuditLog;
import auditLogs.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogResponse> getLogs(
            String tableName,
            String actionType,
            Long actorId,
            Pageable pageable
    ) {
        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tableName != null && !tableName.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tableName")),
                                "%" + tableName.toLowerCase() + "%"
                        )
                );
            }

            if (actionType != null && !actionType.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("actionType"),
                                actionType
                        )
                );
            }

            if (actorId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("actorId"),
                                actorId
                        )
                );
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };

        return auditLogRepository.findAll(spec, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional
    public Long createLog(
            AuditLogRequest request
    ) {
        AuditLog auditLog = AuditLog.builder()
                .tableName(request.tableName())
                .recordId(request.recordId() == null ? 0L : request.recordId())
                .actionType(request.actionType())
                .beforeData(request.beforeData())
                .afterData(request.afterData())
                .actorId(request.actorId())
                .build();

        return auditLogRepository.save(auditLog).getLogId();
    }
}
