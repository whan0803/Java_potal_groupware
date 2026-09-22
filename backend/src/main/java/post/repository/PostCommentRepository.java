package post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import post.entity.PostComment;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    @Query("""
            select c from PostComment c join fetch c.writer
                    where c.post.postId = :postId
                    order by c.createdAt asc, c.commentId asc
        """)

    List<PostComment> findAllByPostId(@Param("postId") Long postId);
}
