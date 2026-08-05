package boards.controller;


import boards.dto.BoardCreateRequest;
import boards.dto.BoardDisableRequest;
import boards.dto.BoardResponse;
import boards.dto.BoardUpdateRequest;
import boards.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    //게시판목록 조회
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards() {
        return ResponseEntity.ok(
                boardService.getBoards()
        );
    }

    //게시판 상세조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(
            @PathVariable Long boardId
    ) {
        return ResponseEntity.ok(
                boardService.getBoard(boardId)
        );
    }

    //게시판 등록
    @PostMapping
    public ResponseEntity<Long> createBoard(
            @Valid @RequestBody BoardCreateRequest request
            ){
        Long boardId = boardService.createBoard(request);

        return ResponseEntity
                .created(URI.create("/api/boards/" + boardId))
                .body(boardId);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request
    ){
        boardService.updateBoard(boardId, request);

        return ResponseEntity.noContent().build();
    }

    // 게시판 사용 중지
    @PatchMapping("/{boardId}/disable")
    public ResponseEntity<Void> disableBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardDisableRequest request
    ) {
        boardService.disableBoard(
                boardId,
                request.userId()
        );

        return ResponseEntity.noContent().build();
    }
}
