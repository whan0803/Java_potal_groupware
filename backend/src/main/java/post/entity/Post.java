package post.entity;

import boards.entity.Board;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "Text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static Post create(
            Board board,
            String title,
            String content,
            User writer
    ) {
        Post post = new Post();

        post.board = board;
        post.title = title;
        post.content = content;
        post.writer = writer;
        post.viewCount = 0;
        post.useYn = "Y";
        post.createdAt = LocalDateTime.now();
        post.createdBy = writer.getUserId();

        return post;
    }

    public void update(
            String title,
            String content,
            Long userId
    ){
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void increaseViewCount(){
        this.viewCount++;
    }

    public void delete(Long userId){
        this.useYn = "N";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public boolean isWriter(Long userid) {
        return writer.getUserId().equals(userid);
    }
}
