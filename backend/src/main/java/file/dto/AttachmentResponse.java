package file.dto;

import file.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(

        Long attachmentId,
        String originalName,
        String storedName,

        Long fileSize,
        String fileExtension,
        String referenceType,
        Long referenceId,
        LocalDateTime createAt

) {

    public static AttachmentResponse from(
            Attachment attachment
    ){
        return new AttachmentResponse(
                attachment.getAttachmentId(),
                attachment. getOriginalName(),
                attachment.getStoredName(),
                attachment.getFileSize(),
                attachment.getFileExtension(),
                attachment.getReferenceType(),
                attachment.getReferenceId(),
                attachment.getCreatedAt()
        );
    }
}
