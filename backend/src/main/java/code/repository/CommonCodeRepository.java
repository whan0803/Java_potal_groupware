package code.repository;

import code.entity.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommonCodeRepository
        extends JpaRepository<CommonCode, String> {


    boolean existsByCodeGroupId(
            String codeGroupId
    );

}