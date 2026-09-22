package post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import post.dto.PostCommentCreateRequest;
import post.dto.PostCommentResponse;
import post.service.PostCommentService;
import security.CustomUserDetails;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentController {

    private final PostCommentService commentService;

    @GetMapping
    public ResponseEntity<List<PostCommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        List<PostCommentResponse> response =
                commentService.getComments(postId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PostCommentResponse> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PostCommentCreateRequest request
    ) {
        PostCommentResponse response =
                commentService.createComment(
                        postId,
                        user.getUserId(),
                        request
                );

        URI location = URI.create(
                "/api/posts/"
                        + postId
                        + "/comments/"
                        + response.commentId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        commentService.deleteComment(
                postId,
                commentId,
                user.getUserId()
        );

        return ResponseEntity.noContent().build();
    }
}
