package notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import notice.dto.*;
import notice.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;


    @GetMapping
    public ResponseEntity<Page<NoticeListResponse>> getNotices(

            @RequestParam(required = false)
            String searchType,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "true")
            Boolean visibleOnly,

            @PageableDefault(
                    size = 10,
                    direction = Sort.Direction.DESC,
                    sort = {
                            "importantYn",
                            "createdAt"
                    }
            )
            Pageable pageable
    ) {
        NoticeSearchCondition condition =
                new NoticeSearchCondition(
                        searchType,
                        keyword,
                        visibleOnly
                );

        Page<NoticeListResponse> result =
                noticeService.getNotices(
                        condition,
                        pageable
                );

        return ResponseEntity.ok(result);
    }

    // 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNotice(
            @PathVariable Long noticeId,
            @RequestParam(defaultValue = "true") boolean increaseView,
            @RequestParam(defaultValue = "true") boolean requireVisible
    ) {
        return ResponseEntity.ok(
                noticeService.getNotice(
                        noticeId,
                        increaseView,
                        requireVisible
                )
        );
    }

    // 등록
    @PostMapping
    public ResponseEntity<Long> createNotice(
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        Long noticeId =
                noticeService.createNotice(request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/notices/" + noticeId
                        )
                )
                .body(noticeId);
    }

    //수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        noticeService.updateNotice(
                noticeId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    // 논리 삭제
    @PatchMapping("/{noticeId}/delete")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeDeleteRequest request
    ) {
        noticeService.deleteNotice(
                noticeId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    //중요 공지 설정
    @PatchMapping("/{noticeId}/important")
    public ResponseEntity<Void> changeImportant(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeImportantRequest request
    ) {
        noticeService.changeImportant(
                noticeId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}
