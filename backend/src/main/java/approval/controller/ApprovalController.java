package approval.controller;

import approval.dto.*;
import approval.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;


    // 결재문서 목록 / 검색
    @GetMapping
    public ResponseEntity<Page<ApprovalListResponse>>
    getDocuments(

            @RequestParam(required = false)
            String title,

            @RequestParam(required = false)
            Long drafterId,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            LocalDateTime startDate,

            @RequestParam(required = false)
            LocalDateTime endDate,

            Pageable pageable
    ) {

        return ResponseEntity.ok(
                approvalService.getDocuments(
                        title,
                        drafterId,
                        status,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }


    // 작성 / 임시저장
    @PostMapping
    public ResponseEntity<Long> createDraft(

            @Valid
            @RequestBody
            ApprovalCreateRequest request
    ) {

        return ResponseEntity.ok(
                approvalService.createDraft(
                        request
                )
        );
    }


    // 임시저장 수정
    @PutMapping("/{documentId}")
    public ResponseEntity<Void> updateDraft(

            @PathVariable
            Long documentId,

            @Valid
            @RequestBody
            ApprovalCreateRequest request
    ) {

        approvalService.updateDraft(
                documentId,
                request
        );

        return ResponseEntity.ok().build();
    }


    // 상세조회
    @GetMapping("/{documentId}")
    public ResponseEntity<ApprovalDetailResponse>
    getDocument(

            @PathVariable
            Long documentId
    ) {

        return ResponseEntity.ok(
                approvalService.getDocument(
                        documentId
                )
        );
    }


    // 결재선 설정
    @PutMapping("/{documentId}/lines")
    public ResponseEntity<Void> setApprovalLines(

            @PathVariable
            Long documentId,

            @RequestParam
            Long userId,

            @Valid
            @RequestBody
            List<ApprovalLineRequest> requests
    ) {

        approvalService.setApprovalLines(
                documentId,
                userId,
                requests
        );

        return ResponseEntity.ok().build();
    }


    // 결재 상신
    @PatchMapping("/{documentId}/submit")
    public ResponseEntity<Void> submit(

            @PathVariable
            Long documentId,

            @Valid
            @RequestBody
            ApprovalSubmitRequest request
    ) {

        approvalService.submit(
                documentId,
                request.userId()
        );

        return ResponseEntity.ok().build();
    }


    // 결재 승인
    @PatchMapping("/{documentId}/approve")
    public ResponseEntity<Void> approve(

            @PathVariable
            Long documentId,

            @Valid
            @RequestBody
            ApprovalProcessRequest request
    ) {

        approvalService.approve(
                documentId,
                request
        );

        return ResponseEntity.ok().build();
    }


    // 결재 반려
    @PatchMapping("/{documentId}/reject")
    public ResponseEntity<Void> reject(

            @PathVariable
            Long documentId,

            @Valid
            @RequestBody
            ApprovalProcessRequest request
    ) {

        approvalService.reject(
                documentId,
                request
        );

        return ResponseEntity.ok().build();
    }


    // 결재 취소
    @PatchMapping("/{documentId}/cancel")
    public ResponseEntity<Void> cancel(

            @PathVariable
            Long documentId,

            @Valid
            @RequestBody
            ApprovalSubmitRequest request
    ) {

        approvalService.cancel(
                documentId,
                request.userId()
        );

        return ResponseEntity.ok().build();
    }


    // 결재 이력
    @GetMapping("/{documentId}/history")
    public ResponseEntity<List<ApprovalLineResponse>>
    getHistory(

            @PathVariable
            Long documentId
    ) {

        return ResponseEntity.ok(
                approvalService.getHistory(
                        documentId
                )
        );
    }
}
