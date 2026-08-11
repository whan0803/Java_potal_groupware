package code.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Entity
@Table(name = "common_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode {


    @Id
    @Column(name = "code_group_id", length = 30)
    private String codeGroupId;


    @Column(name = "code_group_name", nullable = false, length = 100)
    private String codeGroupName;


    @Column(name = "description", length = 255)
    private String description;


    @Column(name = "use_yn", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String useYn;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "created_by")
    private Long createdBy;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "updated_by")
    private Long updatedBy;



    @PrePersist
    public void prePersist(){

        createdAt = LocalDateTime.now();

        if(useYn == null){
            useYn = "Y";
        }

    }


    @PreUpdate
    public void preUpdate(){

        updatedAt = LocalDateTime.now();

    }



    public static CommonCode create(

            String codeGroupId,

            String codeGroupName,

            String description,

            String useYn,

            Long createdBy

    ){

        CommonCode code =
                new CommonCode();


        code.codeGroupId = codeGroupId;

        code.codeGroupName = codeGroupName;

        code.description = description;

        code.useYn = useYn;

        code.createdBy = createdBy;


        return code;

    }





    public void update(

            String codeGroupName,

            String description,

            String useYn,

            Long updatedBy

    ){

        this.codeGroupName = codeGroupName;

        this.description = description;

        this.useYn = useYn;

        this.updatedBy = updatedBy;

    }

}
