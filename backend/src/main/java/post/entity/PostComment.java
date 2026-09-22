package post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.OffsetDateTime;

@Entity
@Table(name = "post_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @Column(name = "context", nullable = false, columnDefinition = "text")
    private String context;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;

    public static PostComment create(
            Post post,
            User writer,
            PostComment parentComment,
            String context
    ){
        PostComment comment = new PostComment();
        comment.post = post;
        comment.writer = writer;
        comment.parentComment = parentComment;
        comment.context = context;
        comment.createdAt = OffsetDateTime.now();
        comment.useYn = "Y";
        return comment;
    }

    public void delete() {
        this.useYn = "N";
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isWriter(Long userId){
        return writer.getUserId().equals(userId);
    }



}
