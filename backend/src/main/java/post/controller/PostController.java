package post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import post.dto.PostCreateRequest;
import post.dto.PostDeleteRequest;
import post.dto.PostDetailResponse;
import post.dto.PostListResponse;
import post.dto.PostSearchCondition;
import post.dto.PostUpdateRequest;
import post.service.PostService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 목록 조회, 검색, 페이징
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPosts(

            @RequestParam(required = false)
            Long boardId,

            @RequestParam(required = false)
            String searchType,

            @RequestParam(required = false)
            String keyword,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt"
            )
            Pageable pageable
    ) {
        PostSearchCondition condition =
                new PostSearchCondition(
                        boardId,
                        searchType,
                        keyword
                );

        Page<PostListResponse> result =
                postService.getPosts(condition, pageable);

        return ResponseEntity.ok(result);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long postId
    ) {
        PostDetailResponse response =
                postService.getPost(postId);

        return ResponseEntity.ok(response);
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<Long> createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        Long postId = postService.createPost(request);

        return ResponseEntity
                .created(
                        URI.create("/api/posts/" + postId)
                )
                .body(postId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(postId, request);

        return ResponseEntity.noContent().build();
    }

    // 게시글 논리 삭제
    @PatchMapping("/{postId}/delete")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostDeleteRequest request
    ) {
        postService.deletePost(postId, request);

        return ResponseEntity.noContent().build();
    }
}