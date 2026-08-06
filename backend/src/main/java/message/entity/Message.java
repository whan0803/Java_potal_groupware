package message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String readYn;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sender_delete_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String senderDeleteYn;

    @Column(name = "receiver_delete_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String receiverDeleteYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Message(
            User sender,
            User receiver,
            String title,
            String content
    ){
        this.sender = sender;
        this.receiver = receiver;
        this.title = title;
        this.content = content;
        this.readYn = "N";
        this.senderDeleteYn = "N";
        this.receiverDeleteYn = "N";
        this.createdAt = LocalDateTime.now();
    }

    public static Message create(
            User sender,
            User receiver,
            String title,
            String content
    ){
        return new Message(
                sender,
                receiver,
                title,
                content
        );
    }

    public void read(){
        if("N".equals(this.readYn)){
            this.readYn = "Y";
            this.readAt = LocalDateTime.now();
        }
    }

    public void deleteBySender() {
        this.senderDeleteYn = "Y";
    }

    public void deleteByReceiver() {
        this.receiverDeleteYn = "Y";
    }
}
