package post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import post.entity.PostReaction;

import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostPostIdAndUserUserId(Long postId, Long userId);
}
