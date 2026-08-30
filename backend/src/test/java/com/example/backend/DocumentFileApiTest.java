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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentFileApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    void setUp() {
        cleanUp();
        userId = createUser();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void documentTemplate_createSearchDetailUpdateAndDelete_flow() throws Exception {
        MockHttpSession session = login();

        Long templateId = createTemplate(
                session,
                "codex_doc_template_001",
                "codex_doc_template_name",
                "codex document template content",
                "Y"
        );

        mockMvc.perform(get("/api/document-templates")
                        .session(session)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.templateId == %d)]".formatted(templateId)).exists());

        mockMvc.perform(get("/api/document-templates/search")
                        .session(session)
                        .param("templateName", "codex_doc_template")
                        .param("templateCode", "codex_doc_template_001")
                        .param("useYn", "Y")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].templateId").value(templateId));

        mockMvc.perform(get("/api/document-templates/{templateId}", templateId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value(templateId))
                .andExpect(jsonPath("$.templateCode").value("codex_doc_template_001"))
                .andExpect(jsonPath("$.templateName").value("codex_doc_template_name"));

        mockMvc.perform(put("/api/document-templates/{templateId}", templateId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateName": "codex_doc_template_name_updated",
                                  "templateDescription": "updated description",
                                  "templateContent": "updated content",
                                  "useYn": "Y",
                                  "updatedBy": %d
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/document-templates/{templateId}", templateId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateName").value("codex_doc_template_name_updated"))
                .andExpect(jsonPath("$.templateContent").value("updated content"));

        mockMvc.perform(patch("/api/document-templates/{templateId}/deactivate", templateId)
                        .session(session)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/document-templates/{templateId}", templateId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useYn").value("N"));

        Long deleteTargetId = createTemplate(
                session,
                "codex_doc_template_delete",
                "codex_doc_template_delete_name",
                "codex document template delete content",
                "Y"
        );

        mockMvc.perform(delete("/api/document-templates/{templateId}", deleteTargetId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/document-templates/{templateId}", deleteTargetId)
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("문서양식을 찾을 수 없습니다."));
    }

    @Test
    void documentTemplate_rejectsDuplicateCodeAndInvalidUseYn() throws Exception {
        MockHttpSession session = login();

        createTemplate(
                session,
                "codex_doc_template_duplicate",
                "codex duplicate",
                "duplicate content",
                "Y"
        );

        mockMvc.perform(post("/api/document-templates")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateCode": "codex_doc_template_duplicate",
                                  "templateName": "codex duplicate again",
                                  "templateContent": "duplicate content",
                                  "useYn": "Y",
                                  "createdBy": %d
                                }
                                """.formatted(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 양식 코드입니다"));

        mockMvc.perform(post("/api/document-templates")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateCode": "codex_doc_template_invalid",
                                  "templateName": "codex invalid",
                                  "templateContent": "invalid content",
                                  "useYn": "X",
                                  "createdBy": %d
                                }
                                """.formatted(userId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attachment_uploadListDownloadAndDelete_flow() throws Exception {
        MockHttpSession session = login();
        Long referenceId = 990001L;

        Long attachmentId = uploadFile(
                session,
                referenceId,
                "codex_file_test.pdf",
                "codex file content"
        );

        mockMvc.perform(get("/api/attachments")
                        .session(session)
                        .param("referenceType", "POST")
                        .param("referenceId", referenceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].attachmentId").value(attachmentId))
                .andExpect(jsonPath("$[0].originalName").value("codex_file_test.pdf"));

        mockMvc.perform(get("/api/attachments/{attachmentId}/download", attachmentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"codex_file_test.pdf\""
                ))
                .andExpect(content().bytes("codex file content".getBytes()));

        mockMvc.perform(delete("/api/attachments/{attachmentId}", attachmentId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attachments")
                        .session(session)
                        .param("referenceType", "POST")
                        .param("referenceId", referenceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/attachments/{attachmentId}/download", attachmentId)
                        .session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("삭제된 파일입니다."));
    }

    @Test
    void attachment_rejectsInvalidExtensionAndReferenceType() throws Exception {
        MockHttpSession session = login();

        MockMultipartFile invalidExtensionFile =
                new MockMultipartFile(
                        "file",
                        "codex_file_test.exe",
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        "bad".getBytes()
                );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(invalidExtensionFile)
                        .session(session)
                        .param("referenceType", "POST")
                        .param("referenceId", "990002")
                        .param("userId", userId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("허용되지 않은 파일 형식입니다."));

        MockMultipartFile invalidReferenceFile =
                new MockMultipartFile(
                        "file",
                        "codex_file_test.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        "content".getBytes()
                );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(invalidReferenceFile)
                        .session(session)
                        .param("referenceType", "INVALID")
                        .param("referenceId", "990003")
                        .param("userId", userId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("허용되지 않은 첨부 대상입니다."));
    }

    @Test
    void documentAndAttachment_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/document-templates"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/attachments")
                        .param("referenceType", "POST")
                        .param("referenceId", "1"))
                .andExpect(status().isUnauthorized());
    }

    private Long createTemplate(
            MockHttpSession session,
            String templateCode,
            String templateName,
            String templateContent,
            String useYn
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/document-templates")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateCode": "%s",
                                  "templateName": "%s",
                                  "templateDescription": "codex template description",
                                  "templateContent": "%s",
                                  "useYn": "%s",
                                  "createdBy": %d
                                }
                                """.formatted(
                                templateCode,
                                templateName,
                                templateContent,
                                useYn,
                                userId
                        )))
                .andExpect(status().isOk())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString());
    }

    private Long uploadFile(
            MockHttpSession session,
            Long referenceId,
            String originalName,
            String content
    ) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        originalName,
                        MediaType.APPLICATION_PDF_VALUE,
                        content.getBytes()
                );

        MvcResult result = mockMvc.perform(multipart("/api/attachments/upload")
                        .file(file)
                        .session(session)
                        .param("referenceType", "POST")
                        .param("referenceId", referenceId.toString())
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        Number attachmentId = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.attachmentId"
        );

        return attachmentId.longValue();
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "codex_doc_file_user",
                                  "password": "AuthTest!123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private Long createUser() {
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
                        values (
                            'codex_doc_file_user',
                            ?,
                            'Codex Doc File User',
                            'codex_doc_file_user@example.com',
                            'Y',
                            now()
                        )
                        returning user_id
                        """,
                Long.class,
                PASSWORD_HASH
        );
    }

    private void cleanUp() {
        deleteUploadedFiles();

        jdbcTemplate.update(
                "delete from attachments where original_name like 'codex_file_test%'"
        );
        jdbcTemplate.update(
                "delete from document_templates where template_code like 'codex_doc_template%'"
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id = 'codex_doc_file_user'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id = 'codex_doc_file_user'"
        );
    }

    private void deleteUploadedFiles() {
        List<String> paths =
                jdbcTemplate.queryForList(
                        """
                                select file_path
                                from attachments
                                where original_name like 'codex_file_test%'
                                """,
                        String.class
                );

        for (String path : paths) {
            try {
                Files.deleteIfExists(Path.of(path));
            } catch (Exception ignored) {
            }
        }
    }
}
