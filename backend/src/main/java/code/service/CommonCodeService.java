package code.service;


import code.dto.*;
import code.entity.CommonCode;
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



    // CODE-001
    // 코드 그룹 목록 조회

    public List<CommonCodeResponse> getList(){


        return repository.findAll()

                .stream()

                .map(CommonCodeResponse::from)

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

    }

}