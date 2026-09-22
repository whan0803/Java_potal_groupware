package post.dto;

import post.entity.PostComment;

import java.time.OffsetDateTime;

public record PostCommentResponse(
        Long commentId,
        Long postId,
        Long parentCommentId,
        Long writerId,
        String writerName,
        String context,
        OffsetDateTime createdAt,
        boolean deleted
) {
    public static PostCommentResponse from(PostComment comment) {
        boolean deleted = "N".equals(comment.getUseYn());

        return new PostCommentResponse(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getParentComment() == null ? null : comment.getParentComment().getCommentId(),
                comment.getWriter().getUserId(),
                comment.getWriter().getUserName(),
                deleted ? "삭제된 댓글입니다" : comment.getContext(),
                comment.getCreatedAt(),
                deleted
        );
    }
}
