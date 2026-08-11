package com.example.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApprovalApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long drafterId;
    private Long firstApproverId;
    private Long secondApproverId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        cleanUp();
        drafterId = createUser(
                "codex_approval_drafter",
                "codex_approval_drafter@example.com",
                "Codex Approval Drafter"
        );
        firstApproverId = createUser(
                "codex_approval_first",
                "codex_approval_first@example.com",
                "Codex Approval First"
        );
        secondApproverId = createUser(
                "codex_approval_second",
                "codex_approval_second@example.com",
                "Codex Approval Second"
        );
        otherUserId = createUser(
                "codex_approval_other",
                "codex_approval_other@example.com",
                "Codex Approval Other"
        );
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void createSubmitApproveAndHistory_flow() throws Exception {
        MockHttpSession session = login("codex_approval_drafter");
        Long documentId = createDraft(
                session,
                "codex_approval_api_title",
                "codex approval api content"
        );

        mockMvc.perform(get("/api/approvals/{documentId}", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalDocumentId").value(documentId))
                .andExpect(jsonPath("$.drafterId").value(drafterId))
                .andExpect(jsonPath("$.approvalStatus").value("DRAFT"))
                .andExpect(jsonPath("$.approvalLines", hasSize(2)))
                .andExpect(jsonPath("$.approvalLines[0].approvalStatus").value("WAITING"));

        mockMvc.perform(get("/api/approvals")
                        .session(session)
                        .param("title", "codex_approval_api")
                        .param("drafterId", drafterId.toString())
                        .param("status", "DRAFT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].approvalDocumentId").value(documentId));

        mockMvc.perform(patch("/api/approvals/{documentId}/submit", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(drafterId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}/history", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].approvalStatus").value("WAITING"));

        mockMvc.perform(patch("/api/approvals/{documentId}/approve", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": %d,
                                  "comment": "first approved"
                                }
                                """.formatted(firstApproverId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}/history", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$[1].approvalStatus").value("PENDING"));

        mockMvc.perform(patch("/api/approvals/{documentId}/approve", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": %d,
                                  "comment": "second approved"
                                }
                                """.formatted(secondApproverId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.approvalLines[0].approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.approvalLines[1].approvalStatus").value("APPROVED"));
    }

    @Test
    void updateDraftAndSetApprovalLines_flow() throws Exception {
        MockHttpSession session = login("codex_approval_drafter");
        Long documentId = createDraftWithoutLines(
                session,
                "codex_approval_api_update",
                "draft content"
        );

        mockMvc.perform(put("/api/approvals/{documentId}", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drafterId": %d,
                                  "title": "codex_approval_api_updated",
                                  "content": "updated content",
                                  "approvalLines": []
                                }
                                """.formatted(drafterId)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/approvals/{documentId}/lines", documentId)
                        .session(session)
                        .param("userId", drafterId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "approverId": %d,
                                    "approvalOrder": 1,
                                    "approvalType": "APPROVAL"
                                  }
                                ]
                                """.formatted(firstApproverId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("codex_approval_api_updated"))
                .andExpect(jsonPath("$.approvalLines", hasSize(1)))
                .andExpect(jsonPath("$.approvalLines[0].approverId").value(firstApproverId));
    }

    @Test
    void reject_requiresCommentAndCurrentApprover() throws Exception {
        MockHttpSession session = login("codex_approval_drafter");
        Long documentId = createDraft(
                session,
                "codex_approval_api_reject",
                "reject content"
        );

        submit(session, documentId);

        mockMvc.perform(patch("/api/approvals/{documentId}/reject", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": %d,
                                  "comment": ""
                                }
                                """.formatted(firstApproverId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("반려 의견은 필수입니다."));

        mockMvc.perform(patch("/api/approvals/{documentId}/reject", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": %d,
                                  "comment": "not my turn"
                                }
                                """.formatted(secondApproverId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 결재 순서의 결재자가 아닙니다."));

        mockMvc.perform(patch("/api/approvals/{documentId}/reject", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId": %d,
                                  "comment": "rejected"
                                }
                                """.formatted(firstApproverId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
    }

    @Test
    void cancel_rejectsNonDrafter() throws Exception {
        MockHttpSession session = login("codex_approval_drafter");
        Long documentId = createDraft(
                session,
                "codex_approval_api_cancel",
                "cancel content"
        );

        mockMvc.perform(patch("/api/approvals/{documentId}/cancel", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(otherUserId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("작성자만 결재를 취소할 수 있습니다."));

        mockMvc.perform(patch("/api/approvals/{documentId}/cancel", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(drafterId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/approvals/{documentId}", documentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("CANCELED"));
    }

    @Test
    void createDraft_rejectsDuplicateApprover() throws Exception {
        MockHttpSession session = login("codex_approval_drafter");

        mockMvc.perform(post("/api/approvals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drafterId": %d,
                                  "title": "codex_approval_api_duplicate",
                                  "content": "duplicate content",
                                  "approvalLines": [
                                    {
                                      "approverId": %d,
                                      "approvalOrder": 1,
                                      "approvalType": "APPROVAL"
                                    },
                                    {
                                      "approverId": %d,
                                      "approvalOrder": 2,
                                      "approvalType": "APPROVAL"
                                    }
                                  ]
                                }
                                """.formatted(
                                drafterId,
                                firstApproverId,
                                firstApproverId
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("동일한 결재자를 중복 지정할 수 없습니다."));
    }

    @Test
    void approvals_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/approvals"))
                .andExpect(status().isUnauthorized());
    }

    private Long createDraft(
            MockHttpSession session,
            String title,
            String content
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/approvals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drafterId": %d,
                                  "title": "%s",
                                  "content": "%s",
                                  "approvalLines": [
                                    {
                                      "approverId": %d,
                                      "approvalOrder": 1,
                                      "approvalType": "APPROVAL"
                                    },
                                    {
                                      "approverId": %d,
                                      "approvalOrder": 2,
                                      "approvalType": "APPROVAL"
                                    }
                                  ]
                                }
                                """.formatted(
                                drafterId,
                                title,
                                content,
                                firstApproverId,
                                secondApproverId
                        )))
                .andExpect(status().isOk())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString());
    }

    private Long createDraftWithoutLines(
            MockHttpSession session,
            String title,
            String content
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/approvals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drafterId": %d,
                                  "title": "%s",
                                  "content": "%s"
                                }
                                """.formatted(drafterId, title, content)))
                .andExpect(status().isOk())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString());
    }

    private void submit(
            MockHttpSession session,
            Long documentId
    ) throws Exception {
        mockMvc.perform(patch("/api/approvals/{documentId}/submit", documentId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(drafterId)))
                .andExpect(status().isOk());
    }

    private MockHttpSession login(String loginId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "AuthTest!123"
                                }
                                """.formatted(loginId)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private Long createUser(
            String loginId,
            String email,
            String userName
    ) {
        return jdbcTemplate.queryForObject(
                """
                        insert into users (
                            login_id,
                            password,
                            user_name,
                            email,
                            use_yn,
                            created_at
                        )
                        values (?, ?, ?, ?, 'Y', now())
                        returning user_id
                        """,
                Long.class,
                loginId,
                PASSWORD_HASH,
                userName,
                email
        );
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from approval_lines
                        where approval_document_id in (
                            select approval_document_id
                            from approval_documents
                            where drafter_id in (
                                select user_id
                                from users
                                where login_id like 'codex_approval_%'
                            )
                        )
                        or approver_id in (
                            select user_id
                            from users
                            where login_id like 'codex_approval_%'
                        )
                        """
        );
        jdbcTemplate.update(
                """
                        delete from approval_documents
                        where drafter_id in (
                            select user_id
                            from users
                            where login_id like 'codex_approval_%'
                        )
                        """
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_approval_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id like 'codex_approval_%'"
        );
    }
}
