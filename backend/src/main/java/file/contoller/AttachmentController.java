package file.contoller;

import file.dto.AttachmentResponse;
import file.entity.Attachment;
import file.service.AttachmentService;

import lombok.RequiredArgsConstructor;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;



@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {



    private final AttachmentService service;




    // 업로드

    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponse> upload(

            @RequestParam MultipartFile file,

            @RequestParam String referenceType,

            @RequestParam Long referenceId,

            @RequestParam Long userId

    ) throws Exception {


        return ResponseEntity.ok(

                service.upload(

                        file,

                        referenceType,

                        referenceId,

                        userId

                )

        );

    }





    // 첨부파일 목록 조회

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> list(

            @RequestParam String referenceType,

            @RequestParam Long referenceId

    ){

        return ResponseEntity.ok(

                service.getFiles(

                        referenceType,

                        referenceId

                )

        );

    }





    // 다운로드

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(

            @PathVariable Long attachmentId

    ) throws Exception {


        Attachment attachment =
                service.getAttachment(
                        attachmentId
                );


        Path path =
                Paths.get(
                        attachment.getFilePath()
                );


        Resource resource =
                new UrlResource(
                        path.toUri()
                );


        return ResponseEntity.ok()

                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + attachment.getOriginalName()
                                + "\""
                )

                .body(resource);

    }





    // 삭제

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(

            @PathVariable Long attachmentId

    ){

        service.delete(
                attachmentId
        );


        return ResponseEntity.ok().build();

    }

}
