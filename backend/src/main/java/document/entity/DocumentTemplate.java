package document.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_code", nullable = false, length = 30)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "template_description", length = 255)
    private String templateDescription;

    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent;

    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn = "Y";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.useYn == null) {
            this.useYn = "Y";
        }
    }
    @PreUpdate
    public void updateDate(){

        this.updatedAt = LocalDateTime.now();

    }



    public static DocumentTemplate create(

            String templateCode,
            String templateName,
            String templateDescription,
            String templateContent,
            String useYn,
            Long createdBy

    ){

        DocumentTemplate template =
                new DocumentTemplate();


        template.templateCode = templateCode;
        template.templateName = templateName;
        template.templateDescription = templateDescription;
        template.templateContent = templateContent;
        template.createdBy = createdBy;
        template.useYn = useYn;


        return template;

    }




    public void update(

            String templateName,
            String templateDescription,
            String templateContent,
            String useYn,
            Long updatedBy

    ){

        this.templateName = templateName;
        this.templateDescription = templateDescription;
        this.templateContent = templateContent;
        this.useYn = useYn;
        this.updatedBy = updatedBy;

    }



    public void delete(){

        this.useYn = "N";

    }
}
