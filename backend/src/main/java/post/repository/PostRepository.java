package post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import post.entity.Post;

public interface PostRepository
        extends JpaRepository<Post, Long>,
        JpaSpecificationExecutor<Post> {

    boolean existsByBoardBoardIdAndUseYn(Long boardId, String useYn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Post post where post.board.boardId = :boardId and post.useYn = 'N'")
    void deleteInactiveByBoardId(@Param("boardId") Long boardId);
}
