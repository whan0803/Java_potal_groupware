package document.controller;


import document.dto.*;
import document.service.DocumentTemplateService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
public class DocumentTemplateController {



    private final DocumentTemplateService service;





    // 목록 조회

    @GetMapping
    public ResponseEntity<Page<DocumentTemplateResponse>> getList(

            Pageable pageable

    ){

        return ResponseEntity.ok(
                service.getList(pageable)
        );

    }







    // 검색

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentTemplateResponse>> search(

            @RequestParam(required = false)
            String templateName,


            @RequestParam(required = false)
            String templateCode,


            @RequestParam(required = false)
            String useYn,


            Pageable pageable

    ){

        return ResponseEntity.ok(

                service.search(
                        templateName,
                        templateCode,
                        useYn,
                        pageable
                )

        );

    }







    // 상세 조회

    @GetMapping("/{templateId}")
    public ResponseEntity<DocumentTemplateResponse> detail(

            @PathVariable Long templateId

    ){

        return ResponseEntity.ok(
                service.getDetail(templateId)
        );

    }







    // 등록

    @PostMapping
    public ResponseEntity<Long> create(

            @Valid
            @RequestBody
            DocumentTemplateCreateRequest request

    ){

        return ResponseEntity.ok(
                service.create(request)
        );

    }







    // 수정

    @PutMapping("/{templateId}")
    public ResponseEntity<Void> update(

            @PathVariable Long templateId,

            @Valid
            @RequestBody
            DocumentTemplateUpdateRequest request

    ){

        service.update(
                templateId,
                request
        );


        return ResponseEntity.ok().build();

    }







    // 삭제

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> delete(

            @PathVariable Long templateId

    ){

        service.delete(templateId);


        return ResponseEntity.ok().build();

    }


}
