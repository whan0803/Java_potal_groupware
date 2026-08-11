package file.service;


import file.dto.AttachmentResponse;
import file.entity.Attachment;
import file.repository.AttachmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.*;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {



    private final AttachmentRepository repository;



    private final String uploadPath =
            "uploads/";



    // 파일 크기 제한
    private final long MAX_FILE_SIZE =
            10 * 1024 * 1024;



    // 허용 확장자
    private final List<String> allowedExtensions =
            List.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "pdf",
                    "hwp",
                    "xlsx",
                    "docx"
            );





    // 파일 업로드

    @Transactional
    public AttachmentResponse upload(

            MultipartFile file,

            String referenceType,

            Long referenceId,

            Long userId

    ) throws IOException {

        validateReferenceType(referenceType);
        validateRequiredReference(referenceId);
        validateRequiredUser(userId);
        validateEmpty(file);
        validateSize(file);



        String originalName =
                file.getOriginalFilename();



        String extension =
                getExtension(originalName);



        validateExtension(extension);



        String storedName =
                UUID.randomUUID()
                        + "."
                        + extension;



        Path path =
                Paths.get(
                        uploadPath,
                        storedName
                );



        Files.createDirectories(
                path.getParent()
        );



        Files.copy(

                file.getInputStream(),

                path,

                StandardCopyOption.REPLACE_EXISTING

        );




        Attachment attachment =
                Attachment.create(

                        referenceType,

                        referenceId,

                        originalName,

                        storedName,

                        path.toString(),

                        file.getSize(),

                        extension,

                        userId

                );



        repository.save(attachment);



        return AttachmentResponse.from(
                attachment
        );

    }





    // 다운로드용 조회

    public Attachment getAttachment(
            Long attachmentId
    ){

        Attachment attachment =
                repository.findById(
                        attachmentId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "파일을 찾을 수 없습니다."
                                )
                );

        if (!"Y".equals(attachment.getUseYn())) {
            throw new IllegalStateException(
                    "삭제된 파일입니다."
            );
        }

        return attachment;

    }





    // 삭제

    @Transactional
    public void delete(
            Long attachmentId
    ){

        Attachment attachment =
                getAttachment(attachmentId);


        attachment.delete();

    }





    // 첨부파일 목록

    public List<AttachmentResponse> getFiles(

            String referenceType,

            Long referenceId

    ){

        return repository
                .findByReferenceTypeAndReferenceIdAndUseYn(

                        referenceType,

                        referenceId,

                        "Y"

                )
                .stream()
                .map(AttachmentResponse::from)
                .toList();

    }






    // 파일 크기 검증

    private void validateSize(
            MultipartFile file
    ){

        if(file.getSize() > MAX_FILE_SIZE){

            throw new IllegalArgumentException(
                    "파일 크기는 10MB 이하만 가능합니다."
            );

        }

    }





    // 확장자 검증

    private void validateExtension(
            String extension
    ){

        if(!allowedExtensions.contains(
                extension.toLowerCase()
        )){

            throw new IllegalArgumentException(
                    "허용되지 않은 파일 형식입니다."
            );

        }

    }




    private String getExtension(
            String filename
    ){

        if(filename == null
                || !filename.contains(".")){

            throw new IllegalArgumentException(
                    "파일 확장자가 없습니다."
            );

        }


        return filename
                .substring(
                        filename.lastIndexOf(".") + 1
                );

    }

    private void validateReferenceType(
            String referenceType
    ) {
        List<String> allowedReferenceTypes =
                List.of(
                        "POST",
                        "NOTICE",
                        "APPROVAL",
                        "TASK",
                        "MESSAGE"
                );

        if (referenceType == null
                || !allowedReferenceTypes.contains(referenceType)) {
            throw new IllegalArgumentException(
                    "허용되지 않은 첨부 대상입니다."
            );
        }
    }

    private void validateRequiredReference(
            Long referenceId
    ) {
        if (referenceId == null) {
            throw new IllegalArgumentException(
                    "첨부 대상 번호는 필수입니다."
            );
        }
    }

    private void validateRequiredUser(
            Long userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "등록자 번호는 필수입니다."
            );
        }
    }

    private void validateEmpty(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "파일은 필수입니다."
            );
        }
    }


}
