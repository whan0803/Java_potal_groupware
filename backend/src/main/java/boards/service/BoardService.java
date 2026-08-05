package boards.service;

import boards.dto.BoardCreateRequest;
import boards.dto.BoardDisableRequest;
import boards.dto.BoardUpdateRequest;
import boards.entity.Board;
import boards.dto.BoardResponse;
import boards.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    //게시판 목록 조회
    public List<BoardResponse> getBoards() {
        return boardRepository
                .findAllByOrderByBoardIdDesc()
                .stream()
                .map(BoardResponse::from)
                .toList();
    }

    //게시판 상세조회
    public BoardResponse getBoard(Long boardId){
        Board board = findBoard(boardId);

        return BoardResponse.from(board);
    }

    //게시판 등록
    @Transactional
    public Long createBoard(BoardCreateRequest request){
        if(boardRepository.existsByBoardName(request.boardName())){
            throw new IllegalArgumentException(
                    "이미 사용 중인 게시판명입니다"
            );
        }
        Board board = Board.create(
                request.boardName(),
                request.boardDescription(),
                request.attachmentYn(),
                request.userId()
        );
        return boardRepository.save(board).getBoardId();
    }

    //게시판 수정
    @Transactional
    public void updateBoard(Long boardId, BoardUpdateRequest request) {
        Board board = findBoard(boardId);

        boolean duplicate =
                boardRepository.existsByBoardNameAndBoardIdNot(
                        request.boardName(),
                        boardId
                );

        if(duplicate) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 게시판명입니다"
            );
        }
        board.update(
                request.boardName(),
                request.boardDescription(),
                request.attachmentYn(),
                request.useYn(),
                request.userId()
        );
    }

    // 게시판 사용 중지
    @Transactional
    public void disableBoard(
            Long boardId,
            Long userId
    ) {
        Board board = findBoard(boardId);

        if ("N".equals(board.getUseYn())) {
            throw new IllegalStateException(
                    "이미 사용 중지된 게시판입니다."
            );
        }

        board.disable(userId);
    }


    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시판을 찾을 수 없습니다. boardId: "
                                        + boardId
                        )
                );
    }
}
