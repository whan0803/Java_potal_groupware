package file.repository;


import file.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface AttachmentRepository
        extends JpaRepository<Attachment, Long> {



    List<Attachment>
    findByReferenceTypeAndReferenceIdAndUseYn(

            String referenceType,

            Long referenceId,

            String useYn

    );


}