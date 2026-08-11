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




    // CODE-001
    // 목록 조회

    @GetMapping
    public ResponseEntity<List<CommonCodeResponse>> getList(){


        return ResponseEntity.ok(
                service.getList()
        );

    }







    // CODE-002
    // 등록

    @PostMapping
    public ResponseEntity<Void> create(

            @RequestBody CommonCodeCreateRequest request

    ){

        service.create(request);


        return ResponseEntity.ok().build();

    }








    // CODE-003
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


}