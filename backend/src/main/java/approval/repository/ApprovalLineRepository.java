package approval.repository;

import approval.entity.ApprovalLine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalLineRepository
        extends JpaRepository<ApprovalLine, Long> {

    List<ApprovalLine>
    findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
            Long approvalDocumentId
    );

    Optional<ApprovalLine>
    findByApprovalDocumentApprovalDocumentIdAndApprovalOrder(
            Long approvalDocumentId,
            Integer approvalOrder
    );

    Optional<ApprovalLine>
    findByApprovalDocumentApprovalDocumentIdAndApproverUserId(
            Long approvalDocumentId,
            Long approverId
    );

    long countByApproverUserIdAndApprovalStatus(
            Long approverId,
            String approvalStatus
    );

    @EntityGraph(attributePaths = {
            "approvalDocument",
            "approvalDocument.drafter"
    })
    List<ApprovalLine> findByApproverUserIdAndApprovalStatusOrderByApprovalDocumentCreatedAtDesc(
            Long approverId,
            String approvalStatus,
            Pageable pageable
    );

    void deleteByApprovalDocumentApprovalDocumentId(
            Long approvalDocumentId
    );
}
