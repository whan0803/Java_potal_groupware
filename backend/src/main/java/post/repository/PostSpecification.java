package post.repository;

import org.springframework.data.jpa.domain.Specification;
import post.entity.Post;

public class PostSpecification {

    private PostSpecification() {
    }

    public static Specification<Post> useYnEquals(String useYn) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("useYn"),
                        useYn
                );
    }

    public static Specification<Post> boardIdEquals(Long boardId) {
        return (root, query, criteriaBuilder) -> {

            if (boardId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("board").get("boardId"),
                    boardId
            );
        };
    }

    public static Specification<Post> search(
            String searchType,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String type =
                    searchType == null || searchType.isBlank()
                            ? "TITLE_CONTENT"
                            : searchType.toUpperCase();

            String searchKeyword =
                    "%" + keyword.trim().toLowerCase() + "%";

            return switch (type) {
                case "TITLE" -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        searchKeyword
                );

                case "CONTENT" -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")),
                        searchKeyword
                );

                case "WRITER" -> criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("writer").get("userName")
                        ),
                        searchKeyword
                );

                case "TITLE_CONTENT" -> criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                searchKeyword
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("content")),
                                searchKeyword
                        )
                );

                default -> throw new IllegalArgumentException(
                        "지원하지 않는 검색 구분입니다: " + type
                );
            };
        };
    }
}