package message.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import message.dto.*;
import message.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;


     // 받은 쪽지 목록 조회

    @GetMapping("/received")
    public ResponseEntity<Page<ReceivedMessageResponse>>
    getReceivedMessages(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        Page<ReceivedMessageResponse> response =
                messageService.getReceivedMessages(
                        userId,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    //보낸 쪽지 목록 조회
    @GetMapping("/sent")
    public ResponseEntity<Page<SentMessageResponse>>
    getSentMessages(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        Page<SentMessageResponse> response =
                messageService.getSentMessages(
                        userId,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    //쪽지 상세 조회 및 읽음 처리
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDetailResponse>
    getMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId
    ) {
        MessageDetailResponse response =
                messageService.getMessage(
                        messageId,
                        userId
                );

        return ResponseEntity.ok(response);
    }

    //쪽지 작성
    @PostMapping
    public ResponseEntity<Long> sendMessage(
            @RequestParam Long senderId,
            @Valid
            @RequestBody MessageSendRequest request
    ) {
        Long messageId =
                messageService.sendMessage(
                        senderId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(messageId);
    }

    //답장
    @PostMapping("/{messageId}/reply")
    public ResponseEntity<Long> replyMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @Valid
            @RequestBody MessageReplyRequest request
    ) {
        Long replyMessageId =
                messageService.replyMessage(
                        messageId,
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(replyMessageId);
    }

    //뱓은 쪽지 삭제
    @DeleteMapping("/{messageId}/received")
    public ResponseEntity<Void>
    deleteReceivedMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId
    ) {
        messageService.deleteReceivedMessage(
                messageId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    //보낸 쪽지 삭제
    @DeleteMapping("/{messageId}/sent")
    public ResponseEntity<Void>
    deleteSentMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId
    ) {
        messageService.deleteSentMessage(
                messageId,
                userId
        );

        return ResponseEntity.noContent().build();
    }
}