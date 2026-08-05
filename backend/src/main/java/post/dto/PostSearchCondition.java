package post.dto;

public record PostSearchCondition(
        Long boardId,
        String searchType,
        String keyword
) {
}
