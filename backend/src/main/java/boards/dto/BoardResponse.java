package boards.dto;

import boards.entity.Board;

import java.time.LocalDateTime;

public record BoardResponse(
        Long boardId,
        String boardName,
        String boardDescription,
        String attachmentYn,
        String useYn,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy
) {

    public static BoardResponse from(Board board){
        return new BoardResponse(
                board.getBoardId(),
                board.getBoardName(),
                board.getBoardDescription(),
                board.getAttachmentYn(),
                board.getUseYn(),
                board.getCreatedAt(),
                board.getCreatedBy(),
                board.getUpdatedAt(),
                board.getUpdatedBy()
        );
    }
}
