package notice.dto;

public record NoticeSearchCondition(
        String searchType,
        String keyword,
        boolean visibleOnly
) {
}
