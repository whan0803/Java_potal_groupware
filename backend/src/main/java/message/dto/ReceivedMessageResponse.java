package message.dto;

import message.entity.Message;

import java.time.LocalDateTime;

public record ReceivedMessageResponse(
        Long messageId,
        Long senderId,
        String senderName,
        String title,
        String readYn,
        LocalDateTime readAt,
        LocalDateTime receivedAt
) {
    public static ReceivedMessageResponse from(Message message) {
        return new ReceivedMessageResponse(
                message.getMessageId(),
                message.getSender().getUserId(),
                message.getSender().getUserName(),
                message.getTitle(),
                message.getReadYn(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
