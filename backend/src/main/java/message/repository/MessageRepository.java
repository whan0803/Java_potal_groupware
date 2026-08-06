package message.repository;

import message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @EntityGraph(attributePaths = {
            "sender",
            "receiver"
    })
    Page<Message> findByReceiverUserIdAndReceiverDeleteYn(
            Long receiverId,
            String receiverDeleteYn,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "sender",
            "receiver"
    })
    Page<Message> findBySenderUserIdAndSenderDeleteYn(
            Long senderId,
            String senderDeleteYn,
            Pageable pageable
    );
    @EntityGraph(attributePaths = {
            "sender",
            "receiver"
    })
    Optional<Message> findMessageByMessageId(
            Long messageId
    );
}
