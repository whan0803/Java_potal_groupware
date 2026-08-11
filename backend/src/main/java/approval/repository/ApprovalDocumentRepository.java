package approval.repository;

import approval.entity.ApprovalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long>,
        JpaSpecificationExecutor<ApprovalDocument> {
}
