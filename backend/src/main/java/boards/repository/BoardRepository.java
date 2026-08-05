package boards.repository;

import boards.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    boolean existsByBoardName(String boardName);

    boolean existsByBoardNameAndBoardIdNot(
            String boardName,
            Long boardId
    );

    List<Board> findAllByOrderByBoardIdDesc();
}
