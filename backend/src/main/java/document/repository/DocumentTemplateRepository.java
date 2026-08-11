package document.repository;

import document.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> ,
        JpaSpecificationExecutor<DocumentTemplate> {

    boolean existsByTemplateCode(
            String templateCode
    );
}
