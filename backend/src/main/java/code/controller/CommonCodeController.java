package code.controller;


import code.dto.*;
import code.service.CommonCodeService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {


    private final CommonCodeService service;




    // 목록 조회

    @GetMapping
    public ResponseEntity<List<CommonCodeResponse>> getList(){


        return ResponseEntity.ok(
                service.getList()
        );

    }


    @GetMapping("/{codeGroupId}/details")
    public ResponseEntity<List<CommonCodeDetailResponse>> getDetails(
            @PathVariable String codeGroupId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ){

        return ResponseEntity.ok(
                service.getDetails(codeGroupId, activeOnly)
        );

    }







    // 등록

    @PostMapping
    public ResponseEntity<Void> create(

            @RequestBody CommonCodeCreateRequest request

    ){

        service.create(request);


        return ResponseEntity.ok().build();

    }








    // 수정

    @PutMapping("/{codeGroupId}")
    public ResponseEntity<Void> update(

            @PathVariable String codeGroupId,


            @RequestBody CommonCodeUpdateRequest request

    ){


        service.update(
                codeGroupId,
                request
        );


        return ResponseEntity.ok().build();

    }


    @DeleteMapping("/{codeGroupId}")
    public ResponseEntity<Void> delete(
            @PathVariable String codeGroupId
    ){

        service.delete(codeGroupId);

        return ResponseEntity.noContent().build();

    }


}
