package code.service;


import code.dto.*;
import code.entity.CommonCode;
import code.entity.CommonCodeDetail;
import code.repository.CommonCodeDetailRepository;
import code.repository.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {


    private final CommonCodeRepository repository;
    private final CommonCodeDetailRepository detailRepository;



    // CODE-001
    // 코드 그룹 목록 조회

    public List<CommonCodeResponse> getList(){


        return repository.findAll()
                .stream()
                .map(code -> CommonCodeResponse.from(
                        code,
                        detailRepository.countByCodeGroupId(code.getCodeGroupId()),
                        getDetails(code.getCodeGroupId(), false)
                ))
                .toList();

    }





    // CODE-002
    // 코드 그룹 등록

    @Transactional
    public void create(

            CommonCodeCreateRequest request

    ){


        if(repository.existsByCodeGroupId(
                request.codeGroupId()
        )){

            throw new IllegalArgumentException(
                    "이미 존재하는 코드 그룹입니다."
            );

        }



        CommonCode code =
                CommonCode.create(

                        request.codeGroupId(),

                        request.codeGroupName(),

                        request.description(),

                        request.useYn(),

                        request.createdBy()

                );



        repository.save(code);

        saveDetails(code, request.details(), request.createdBy());

    }







    // CODE-003
    // 코드 그룹 수정

    @Transactional
    public void update(

            String codeGroupId,

            CommonCodeUpdateRequest request

    ){


        CommonCode code =
                repository.findById(codeGroupId)

                        .orElseThrow(

                                () -> new IllegalArgumentException(
                                        "코드 그룹을 찾을 수 없습니다."
                                )

                        );



        code.update(

                request.codeGroupName(),

                request.description(),

                request.useYn(),

                request.updatedBy()

        );

        detailRepository.deleteByCodeGroupId(codeGroupId);
        detailRepository.flush();
        saveDetails(code, request.details(), request.updatedBy());

    }


    @Transactional
    public void delete(String codeGroupId) {
        CommonCode code =
                repository.findById(codeGroupId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "코드 그룹을 찾을 수 없습니다."
                                )
                        );

        detailRepository.deleteByCodeGroupId(codeGroupId);
        detailRepository.flush();
        repository.delete(code);
    }


    public List<CommonCodeDetailResponse> getDetails(String codeGroupId, boolean activeOnly) {
        List<CommonCodeDetail> details = activeOnly
                ? detailRepository.findByCodeGroupIdAndUseYnOrderBySortOrderAscCodeValueAsc(codeGroupId, "Y")
                : detailRepository.findByCodeGroupIdOrderBySortOrderAscCodeValueAsc(codeGroupId);

        return details.stream()
                .map(CommonCodeDetailResponse::from)
                .toList();
    }


    private void saveDetails(
            CommonCode code,
            List<CommonCodeDetailRequest> requests,
            Long userId
    ) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<CommonCodeDetail> details = requests.stream()
                .filter(request -> request.codeValue() != null && !request.codeValue().isBlank())
                .filter(request -> request.codeName() != null && !request.codeName().isBlank())
                .map(request -> CommonCodeDetail.create(
                        code,
                        request.codeValue().trim().toUpperCase(),
                        request.codeName().trim(),
                        request.sortOrder() == null ? 0 : request.sortOrder(),
                        normalizeUseYn(request.useYn()),
                        userId
                ))
                .toList();

        detailRepository.saveAll(details);
    }


    private String normalizeUseYn(String useYn) {
        if (useYn == null || useYn.isBlank()) {
            return "Y";
        }
        return "N".equalsIgnoreCase(useYn.trim()) ? "N" : "Y";
    }

}
