package post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import user.entity.User;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "post_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_reactions_post_user",
                columnNames = {"post_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long reactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    private PostReactionType reactionType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static PostReaction create(
            Post post,
            User user,
            PostReactionType reactionType
    ) {
        PostReaction reaction = new PostReaction();
        reaction.post = post;
        reaction.user = user;
        reaction.reactionType = reactionType;
        reaction.createdAt = OffsetDateTime.now();
        return reaction;
    }

    public void changeType(PostReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
