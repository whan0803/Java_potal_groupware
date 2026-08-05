package boards.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;


    @Column(name = "board_name", nullable = false, length = 100)
    private String boardName;

    @Column(name = "board_description", length = 255)
    private String boardDescription;

    @Column(name = "attachment_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String attachmentYn;

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


    public static Board create(
            String boardName,
            String boardDescription,
            String attachmentYn,
            Long userId
    ) {
        Board board = new Board();

        board.boardName = boardName;
        board.boardDescription = boardDescription;
        board.attachmentYn = attachmentYn;
        board.useYn = "Y";
        board.createdAt = LocalDateTime.now();
        board.createdBy = userId;

        return board;
    }

    public void update(
            String boardName,
            String boardDescription,
            String attachmentYn,
            String useYn,
            Long userId
    ) {
        this.boardName = boardName;
        this.boardDescription = boardDescription;
        this.attachmentYn = attachmentYn;
        this.useYn = useYn;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void disable(Long userId){
        this.useYn = "N";
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}
