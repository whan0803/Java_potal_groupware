package post.dto;

import post.entity.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long postId,
        Long boardId,
        String boardName,
        String title,
        Long writerId,
        String writerName,
        Integer viewCount,
        Integer recommendCount,
        Integer dislikeCount,
        LocalDateTime createdAt
) {

    public static PostListResponse from(Post post) {
        return new PostListResponse(
                post.getPostId(),
                post.getBoard().getBoardId(),
                post.getBoard().getBoardName(),
                post.getTitle(),
                post.getWriter().getUserId(),
                post.getWriter().getUserName(),
                post.getViewCount(),
                post.getRecommendCount(),
                post.getDislikeCount(),
                post.getCreatedAt()
        );
    }
}