package file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Entity
@Table(name = "attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;


    @Column(name = "reference_type", nullable = false, length = 20)
    private String referenceType;


    @Column(name = "reference_id", nullable = false)
    private Long referenceId;


    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;


    @Column(name = "stored_name", nullable = false, unique = true, length = 255)
    private String storedName;


    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;


    @Column(name = "file_size")
    private Long fileSize;


    @Column(name = "file_extension", length = 20)
    private String fileExtension;


    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn = "Y";


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "created_by")
    private Long createdBy;



    @PrePersist
    public void prePersist(){

        createdAt = LocalDateTime.now();

        if(useYn == null){
            useYn = "Y";
        }
    }



    public static Attachment create(

            String referenceType,

            Long referenceId,

            String originalName,

            String storedName,

            String filePath,

            Long fileSize,

            String fileExtension,

            Long createdBy

    ){

        Attachment attachment =
                new Attachment();


        attachment.referenceType = referenceType;

        attachment.referenceId = referenceId;

        attachment.originalName = originalName;

        attachment.storedName = storedName;

        attachment.filePath = filePath;

        attachment.fileSize = fileSize;

        attachment.fileExtension = fileExtension;

        attachment.createdBy = createdBy;


        return attachment;
    }



    public void delete(){

        this.useYn = "N";

    }

}
