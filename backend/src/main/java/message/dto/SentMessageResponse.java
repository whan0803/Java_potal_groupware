package message.dto;

import message.entity.Message;

import java.time.LocalDateTime;

public record SentMessageResponse(
        Long messageId,
        Long receivedId,
        String receivedName,
        String title,
        String readYn,
        LocalDateTime readAt,
        LocalDateTime sentAt
) {
    public static SentMessageResponse from(Message message){
        return new SentMessageResponse(
                message.getMessageId(),
                message.getReceiver().getUserId(),
                message.getReceiver().getUserName(),
                message.getTitle(),
                message.getReadYn(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
