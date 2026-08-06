package message.service;

import lombok.RequiredArgsConstructor;
import message.dto.*;
import message.entity.Message;
import message.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.entity.User;
import user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


     //받은 쪽지 목록 조회

    public Page<ReceivedMessageResponse> getReceivedMessages(
            Long userId,
            Pageable pageable
    ) {
        return messageRepository
                .findByReceiverUserIdAndReceiverDeleteYn(
                        userId,
                        "N",
                        pageable
                )
                .map(ReceivedMessageResponse::from);
    }

    //보낸 쪽지 목록 조회
    public Page<SentMessageResponse> getSentMessages(
            Long userId,
            Pageable pageable
    ) {
        return messageRepository
                .findBySenderUserIdAndSenderDeleteYn(
                        userId,
                        "N",
                        pageable
                )
                .map(SentMessageResponse::from);
    }

    //상세 조회 및 읽음 처리
    @Transactional
    public MessageDetailResponse getMessage(
            Long messageId,
            Long userId
    ) {
        Message message = findMessage(messageId);

        boolean isSender = message.getSender()
                .getUserId()
                .equals(userId);

        boolean isReceiver = message.getReceiver()
                .getUserId()
                .equals(userId);

        if (!isSender && !isReceiver) {
            throw new IllegalArgumentException(
                    "해당 쪽지를 조회할 권한이 없습니다."
            );
        }

        if (isSender &&
                "Y".equals(message.getSenderDeleteYn())) {
            throw new IllegalArgumentException(
                    "보낸 쪽지함에서 삭제된 쪽지입니다."
            );
        }

        if (isReceiver &&
                "Y".equals(message.getReceiverDeleteYn())) {
            throw new IllegalArgumentException(
                    "받은 쪽지함에서 삭제된 쪽지입니다."
            );
        }

        if (isReceiver) {
            message.read();
        }

        return MessageDetailResponse.from(message);
    }

    //쪽지 작성
    @Transactional
    public Long sendMessage(
            Long senderId,
            MessageSendRequest request
    ) {
        User sender = findUser(senderId);

        User receiver = findUser(
               request.receiveId()
        );

        validateDifferentUser(
                sender.getUserId(),
                receiver.getUserId()
        );

        Message message = Message.create(
                sender,
                receiver,
                request.title(),
                request.content()
        );

        Message savedMessage =
                messageRepository.save(message);

        return savedMessage.getMessageId();
    }

    //답장
    @Transactional
    public Long replyMessage(
            Long originalMessageId,
            Long userId,
            MessageReplyRequest request
    ) {
        Message originalMessage =
                findMessage(originalMessageId);

        if (!originalMessage.getReceiver()
                .getUserId()
                .equals(userId)) {
            throw new IllegalArgumentException(
                    "쪽지를 받은 사용자만 답장할 수 있습니다."
            );
        }

        if ("Y".equals(
                originalMessage.getReceiverDeleteYn()
        )) {
            throw new IllegalArgumentException(
                    "삭제된 쪽지에는 답장할 수 없습니다."
            );
        }

        User sender = originalMessage.getReceiver();
        User receiver = originalMessage.getSender();

        Message replyMessage = Message.create(
                sender,
                receiver,
                request.title(),
                request.content()
        );

        Message savedMessage =
                messageRepository.save(replyMessage);

        return savedMessage.getMessageId();
    }

    //받은 쪽지 삭제
    @Transactional
    public void deleteReceivedMessage(
            Long messageId,
            Long userId
    ) {
        Message message = findMessage(messageId);

        if (!message.getReceiver()
                .getUserId()
                .equals(userId)) {
            throw new IllegalArgumentException(
                    "받은 쪽지를 삭제할 권한이 없습니다."
            );
        }

        if ("Y".equals(
                message.getReceiverDeleteYn()
        )) {
            throw new IllegalArgumentException(
                    "이미 삭제된 받은 쪽지입니다."
            );
        }

        message.deleteByReceiver();
    }

    //보낸 쪽지 삭제
    @Transactional
    public void deleteSentMessage(
            Long messageId,
            Long userId
    ) {
        Message message = findMessage(messageId);

        if (!message.getSender()
                .getUserId()
                .equals(userId)) {
            throw new IllegalArgumentException(
                    "보낸 쪽지를 삭제할 권한이 없습니다."
            );
        }

        if ("Y".equals(
                message.getSenderDeleteYn()
        )) {
            throw new IllegalArgumentException(
                    "이미 삭제된 보낸 쪽지입니다."
            );
        }

        message.deleteBySender();
    }

    private Message findMessage(
            Long messageId
    ) {
        return messageRepository
                .findMessageByMessageId(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "쪽지를 찾을 수 없습니다."
                        )
                );
    }

    private User findUser(
            Long userId
    ) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }

    private void validateDifferentUser(
            Long senderId,
            Long receiverId
    ) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "자기 자신에게는 쪽지를 보낼 수 없습니다."
            );
        }
    }
}