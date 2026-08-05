package notice.repository;

import jakarta.persistence.criteria.JoinType;
import notice.entity.Notice;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class NoticeSpecification {

    private NoticeSpecification() {

    }

    public static Specification<Notice> useYnEquals(String useYn) {
        return (root, query, cb) ->
                cb.equal(root.get("useYn"), useYn);
    }

    public static Specification<Notice> visibleOn(
            LocalDate date
    ) {
        return (root, query, cb) -> {

            if (date == null) {
                return cb.conjunction();
            }

            return cb.and(
                    cb.lessThanOrEqualTo(
                            root.get("startDate"),
                            date
                    ),
                    cb.greaterThanOrEqualTo(
                            root.get("endDate"),
                            date
                    )
            );
        };
    }

    public static Specification<Notice> search(
            String searchType,
            String keyword
    ) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String type =
                    searchType == null || searchType.isBlank()
                            ? "TITLE_CONTENT"
                            : searchType.toUpperCase();

            String searchKeyword =
                    "%" + keyword.trim().toLowerCase() + "%";

            return switch (type) {

                case "TITLE" -> cb.like(
                        cb.lower(root.get("title")),
                        searchKeyword
                );

                case "CONTENT" -> cb.like(
                        cb.lower(root.get("content")),
                        searchKeyword
                );

                case "WRITER" -> {
                    var writerJoin =
                            root.join("writer", JoinType.INNER);

                    yield cb.like(
                            cb.lower(writerJoin.get("userName")),
                            searchKeyword
                    );
                }

                case "TITLE_CONTENT" -> cb.or(
                        cb.like(
                                cb.lower(root.get("title")),
                                searchKeyword
                        ),
                        cb.like(
                                cb.lower(root.get("content")),
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
