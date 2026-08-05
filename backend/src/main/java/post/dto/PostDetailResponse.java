package post.dto;

import post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        Long boardId,
        String boardName,
        String title,
        String content,
        Long writerId,
        String writerName,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getPostId(),
                post.getBoard().getBoardId(),
                post.getBoard().getBoardName(),
                post.getTitle(),
                post.getContent(),
                post.getWriter().getUserId(),
                post.getWriter().getUserName(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
