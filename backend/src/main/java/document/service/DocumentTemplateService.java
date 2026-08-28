package document.service;

import document.dto.DocumentTemplateCreateRequest;
import document.dto.DocumentTemplateResponse;
import document.dto.DocumentTemplateUpdateRequest;
import document.entity.DocumentTemplate;
import document.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentTemplateService {

    private final DocumentTemplateRepository repository;

    public Page<DocumentTemplateResponse> getList(
            Pageable pageable
    ){
        return repository.findAll(pageable).map(DocumentTemplateResponse::from);
    }

    public Page<DocumentTemplateResponse> search(

            String templateName,

            String templateCode,

            String useYn,

            Pageable pageable

    ){

        Specification<DocumentTemplate> spec =
                (root, query, cb) -> null;



        if(templateName != null
                && !templateName.isBlank()){

            spec = spec.and(
                    (root, query, cb) ->
                            cb.like(
                                    root.get("templateName"),
                                    "%" + templateName + "%"
                            )
            );

        }



        if(templateCode != null
                && !templateCode.isBlank()){

            spec = spec.and(
                    (root, query, cb) ->
                            cb.like(
                                    root.get("templateCode"),
                                    "%" + templateCode + "%"
                            )
            );

        }



        if(useYn != null
                && !useYn.isBlank()){

            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("useYn"),
                                    useYn
                            )
            );

        }



        return repository
                .findAll(spec,pageable)
                .map(DocumentTemplateResponse::from);

    }

    public DocumentTemplateResponse getDetail(
            Long templateId
    ){
        return DocumentTemplateResponse.from(
                findTemplate(templateId)
        );
    }

    @Transactional
    public Long create(
            DocumentTemplateCreateRequest request
    ){
        if(repository.existsByTemplateCode(
                request.templateCode()
        )){
            throw new IllegalArgumentException(
                    "이미 존재하는 양식 코드입니다"
            );
        }

        DocumentTemplate template =
                DocumentTemplate.create(

                        request.templateCode(),

                        request.templateName(),

                        request.templateDescription(),

                        request.templateContent(),

                        request.useYn(),

                        request.createdBy()

                );



        repository.save(template);



        return template.getTemplateId();
    }

    // 수정

    @Transactional
    public void update(

            Long templateId,

            DocumentTemplateUpdateRequest request

    ){

        DocumentTemplate template =
                findTemplate(templateId);



        template.update(

                request.templateName(),

                request.templateDescription(),

                request.templateContent(),

                request.useYn(),

                request.updatedBy()

        );

    }







    // 삭제

    @Transactional
    public void delete(
            Long templateId,
            Long updatedBy
    ){

        DocumentTemplate template =
                findTemplate(templateId);

        template.delete(updatedBy);

    }


    private DocumentTemplate findTemplate(
            Long templateId
    ){

        return repository.findById(templateId)

                .orElseThrow(

                        () -> new IllegalArgumentException(
                                "문서양식을 찾을 수 없습니다."
                        )

                );

    }
}
