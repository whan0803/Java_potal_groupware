package message.dto;

import message.entity.Message;

import java.time.LocalDateTime;

public record MessageDetailResponse(
        Long messageId,
        Long sendId,
        String senderName,
        Long receivedId,
        String receivedName,

        String title,
        String content,

        String readYn,
        LocalDateTime readAt,
        LocalDateTime createAt
) {

    public static MessageDetailResponse from(
            Message message
    ){
        return new MessageDetailResponse(
                message.getMessageId(),

                message.getSender().getUserId(),
                message.getSender().getUserName(),

                message.getReceiver().getUserId(),
                message.getReceiver().getUserName(),

                message.getTitle(),
                message.getContent(),

                message.getReadYn(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
