package code.repository;

import code.entity.CommonCodeDetail;
import code.entity.CommonCodeDetailId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, CommonCodeDetailId> {

    List<CommonCodeDetail> findByCodeGroupIdOrderBySortOrderAscCodeValueAsc(String codeGroupId);

    List<CommonCodeDetail> findByCodeGroupIdAndUseYnOrderBySortOrderAscCodeValueAsc(
            String codeGroupId,
            String useYn
    );

    void deleteByCodeGroupId(String codeGroupId);

    long countByCodeGroupId(String codeGroupId);

    boolean existsByCodeGroupIdAndCodeValueAndUseYn(
            String codeGroupId,
            String codeValue,
            String useYn
    );

    Optional<CommonCodeDetail> findByCodeGroupIdAndCodeValueAndUseYn(
            String codeGroupId,
            String codeValue,
            String useYn
    );
}
