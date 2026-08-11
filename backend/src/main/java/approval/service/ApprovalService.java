package approval.service;

import approval.dto.*;
import approval.entity.ApprovalDocument;
import approval.entity.ApprovalLine;
import approval.repository.ApprovalDocumentRepository;
import approval.repository.ApprovalLineRepository;
import document.entity.DocumentTemplate;
import document.repository.DocumentTemplateRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.entity.User;
import user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService {

    private final ApprovalDocumentRepository approvalDocumentRepository;
    private final ApprovalLineRepository approvalLineRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final UserRepository userRepository;



    // 결재문서 목록 / 검색

    public Page<ApprovalListResponse> getDocuments(
            String title,
            Long drafterId,
            String approvalStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {

        Specification<ApprovalDocument> spec =
                Specification.where(useYnEqualsY());

        if (title != null && !title.isBlank()) {

            spec = spec.and(
                    titleContains(title)
            );
        }

        if (drafterId != null) {

            spec = spec.and(
                    drafterEquals(drafterId)
            );
        }

        if (approvalStatus != null
                && !approvalStatus.isBlank()) {

            spec = spec.and(
                    statusEquals(approvalStatus)
            );
        }

        if (startDate != null) {

            spec = spec.and(
                    createdAfter(startDate)
            );
        }

        if (endDate != null) {

            spec = spec.and(
                    createdBefore(endDate)
            );
        }

        return approvalDocumentRepository
                .findAll(spec, pageable)
                .map(ApprovalListResponse::from);
    }



    // 상세 조회

    public ApprovalDetailResponse getDocument(
            Long documentId
    ) {

        ApprovalDocument document =
                findDocument(documentId);

        List<ApprovalLine> lines =
                approvalLineRepository
                        .findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
                                documentId
                        );

        return ApprovalDetailResponse.from(
                document,
                lines
        );
    }



    // 결재문서 작성 / 임시저장

    @Transactional
    public Long createDraft(
            ApprovalCreateRequest request
    ) {

        User drafter =
                findUser(request.drafterId());

        DocumentTemplate template = null;

        if (request.templateId() != null) {

            template =
                    documentTemplateRepository
                            .findById(request.templateId())
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "문서양식을 찾을 수 없습니다."
                                            )
                            );

            if (!"Y".equals(template.getUseYn())) {

                throw new IllegalArgumentException(
                        "사용 중지된 문서양식입니다."
                );
            }
        }


        String documentNumber =
                createDocumentNumber();


        ApprovalDocument document =
                ApprovalDocument.create(
                        documentNumber,
                        template,
                        drafter,
                        request.title(),
                        request.content()
                );


        approvalDocumentRepository.save(document);


        if (request.approvalLines() != null
                && !request.approvalLines().isEmpty()) {

            saveApprovalLines(
                    document,
                    request.approvalLines(),
                    drafter.getUserId()
            );
        }

        return document.getApprovalDocumentId();
    }



    // 임시저장 문서 수정

    @Transactional
    public void updateDraft(
            Long documentId,
            ApprovalCreateRequest request
    ) {

        ApprovalDocument document =
                findDocument(documentId);


        if (!document.getDrafter()
                .getUserId()
                .equals(request.drafterId())) {

            throw new IllegalArgumentException(
                    "작성자만 문서를 수정할 수 있습니다."
            );
        }


        DocumentTemplate template = null;

        if (request.templateId() != null) {

            template =
                    documentTemplateRepository
                            .findById(request.templateId())
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "문서양식을 찾을 수 없습니다."
                                            )
                            );
        }


        document.updateDraft(
                template,
                request.title(),
                request.content(),
                request.drafterId()
        );


        approvalLineRepository
                .deleteByApprovalDocumentApprovalDocumentId(
                        documentId
                );


        if (request.approvalLines() != null
                && !request.approvalLines().isEmpty()) {

            saveApprovalLines(
                    document,
                    request.approvalLines(),
                    request.drafterId()
            );
        }
    }



    // 결재선 설정

    @Transactional
    public void setApprovalLines(
            Long documentId,
            Long userId,
            List<ApprovalLineRequest> requests
    ) {

        ApprovalDocument document =
                findDocument(documentId);


        if (!"DRAFT".equals(
                document.getApprovalStatus()
        )) {

            throw new IllegalStateException(
                    "임시저장 상태에서만 결재선을 수정할 수 있습니다."
            );
        }


        if (!document
                .getDrafter()
                .getUserId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "작성자만 결재선을 설정할 수 있습니다."
            );
        }


        approvalLineRepository
                .deleteByApprovalDocumentApprovalDocumentId(
                        documentId
                );


        saveApprovalLines(
                document,
                requests,
                userId
        );
    }




    // 결재 상신

    @Transactional
    public void submit(
            Long documentId,
            Long userId
    ) {

        ApprovalDocument document =
                findDocument(documentId);


        if (!document.getDrafter()
                .getUserId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "작성자만 결재를 상신할 수 있습니다."
            );
        }


        List<ApprovalLine> lines =
                approvalLineRepository
                        .findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
                                documentId
                        );


        if (lines.isEmpty()) {

            throw new IllegalArgumentException(
                    "결재선을 설정해주세요."
            );
        }


        document.submit();



        // 첫 번째 결재자를 현재 결재자로 변경

        lines.getFirst().pending();
    }




    // 결재 승인

    @Transactional
    public void approve(
            Long documentId,
            ApprovalProcessRequest request
    ) {

        ApprovalDocument document =
                findDocument(documentId);


        if (!"IN_PROGRESS".equals(
                document.getApprovalStatus()
        )) {

            throw new IllegalStateException(
                    "진행 중인 결재문서가 아닙니다."
            );
        }


        List<ApprovalLine> lines =
                approvalLineRepository
                        .findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
                                documentId
                        );


        ApprovalLine currentLine =
                findCurrentLine(
                        lines,
                        request.approverId()
                );


        currentLine.approve(
                request.comment(),
                request.approverId()
        );



        // 다음 결재자 검색

        ApprovalLine nextLine =
                lines.stream()
                        .filter(line ->
                                "WAITING".equals(
                                        line.getApprovalStatus()
                                )
                        )
                        .findFirst()
                        .orElse(null);


        if (nextLine == null) {

            // 모든 결재 완료
            document.approveComplete();

        } else {

            // 다음 결재자 활성화
            nextLine.pending();
        }
    }


    // 결재 반려
    @Transactional
    public void reject(
            Long documentId,
            ApprovalProcessRequest request
    ) {

        if (request.comment() == null
                || request.comment().isBlank()) {

            throw new IllegalArgumentException(
                    "반려 의견은 필수입니다."
            );
        }


        ApprovalDocument document =
                findDocument(documentId);


        if (!"IN_PROGRESS".equals(
                document.getApprovalStatus()
        )) {

            throw new IllegalStateException(
                    "진행 중인 결재문서가 아닙니다."
            );
        }


        List<ApprovalLine> lines =
                approvalLineRepository
                        .findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
                                documentId
                        );


        ApprovalLine currentLine =
                findCurrentLine(
                        lines,
                        request.approverId()
                );


        currentLine.reject(
                request.comment(),
                request.approverId()
        );


        document.reject();
    }


    // 결재 취소
    @Transactional
    public void cancel(
            Long documentId,
            Long userId
    ) {

        ApprovalDocument document =
                findDocument(documentId);


        if (!document.getDrafter()
                .getUserId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "작성자만 결재를 취소할 수 있습니다."
            );
        }


        document.cancel();
    }


    // 결재 이력 조회
    public List<ApprovalLineResponse> getHistory(
            Long documentId
    ) {

        findDocument(documentId);


        return approvalLineRepository
                .findByApprovalDocumentApprovalDocumentIdOrderByApprovalOrderAsc(
                        documentId
                )
                .stream()
                .map(ApprovalLineResponse::from)
                .toList();
    }



    // 결재선 저장
    private void saveApprovalLines(
            ApprovalDocument document,
            List<ApprovalLineRequest> requests,
            Long createdBy
    ) {

        if (requests == null
                || requests.isEmpty()) {

            throw new IllegalArgumentException(
                    "결재선이 존재하지 않습니다."
            );
        }


        Set<Long> approverIds =
                new HashSet<>();

        Set<Integer> orders =
                new HashSet<>();


        for (ApprovalLineRequest request : requests) {


            if (!approverIds.add(
                    request.approverId()
            )) {

                throw new IllegalArgumentException(
                        "동일한 결재자를 중복 지정할 수 없습니다."
                );
            }


            if (!orders.add(
                    request.approvalOrder()
            )) {

                throw new IllegalArgumentException(
                        "결재 순서가 중복되었습니다."
                );
            }


            User approver =
                    findUser(
                            request.approverId()
                    );


            ApprovalLine line =
                    ApprovalLine.create(
                            document,
                            approver,
                            request.approvalOrder(),
                            request.approvalType(),
                            createdBy
                    );


            approvalLineRepository.save(line);
        }
    }



    // 현재 결재자 조회
    private ApprovalLine findCurrentLine(
            List<ApprovalLine> lines,
            Long approverId
    ) {

        return lines.stream()
                .filter(line ->
                        line.getApprover()
                                .getUserId()
                                .equals(approverId)
                )
                .filter(line ->
                        "PENDING".equals(
                                line.getApprovalStatus()
                        )
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "현재 결재 순서의 결재자가 아닙니다."
                                )
                );
    }



    private ApprovalDocument findDocument(
            Long documentId
    ) {

        return approvalDocumentRepository
                .findById(documentId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "결재문서를 찾을 수 없습니다."
                                )
                );
    }



    private User findUser(
            Long userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "사용자를 찾을 수 없습니다."
                                )
                );
    }



    // 문서번호 생성
    private String createDocumentNumber() {

        String random =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();

        String date =
                java.time.LocalDate.now()
                        .toString()
                        .replace("-", "");

        return "APR-" + date + "-" + random;
    }



    // 검색 Specification

    private Specification<ApprovalDocument>
    useYnEqualsY() {

        return (root, query, cb) ->
                cb.equal(
                        root.get("useYn"),
                        "Y"
                );
    }


    private Specification<ApprovalDocument>
    titleContains(String title) {

        return (root, query, cb) ->
                cb.like(
                        cb.lower(
                                root.get("title")
                        ),
                        "%" + title.toLowerCase() + "%"
                );
    }


    private Specification<ApprovalDocument>
    statusEquals(String status) {

        return (root, query, cb) ->
                cb.equal(
                        root.get("approvalStatus"),
                        status
                );
    }


    private Specification<ApprovalDocument>
    drafterEquals(Long drafterId) {

        return (root, query, cb) ->
                cb.equal(
                        root.get("drafter")
                                .get("userId"),
                        drafterId
                );
    }


    private Specification<ApprovalDocument>
    createdAfter(LocalDateTime date) {

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        date
                );
    }


    private Specification<ApprovalDocument>
    createdBefore(LocalDateTime date) {

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        date
                );
    }
}
