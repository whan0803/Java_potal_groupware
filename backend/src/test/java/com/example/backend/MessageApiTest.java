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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MessageApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long senderId;
    private Long receiverId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        cleanUp();
        senderId = createUser(
                "codex_message_sender",
                "codex_message_sender@example.com",
                "Codex Message Sender"
        );
        receiverId = createUser(
                "codex_message_receiver",
                "codex_message_receiver@example.com",
                "Codex Message Receiver"
        );
        otherUserId = createUser(
                "codex_message_other",
                "codex_message_other@example.com",
                "Codex Message Other"
        );
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void sendMessage_readReplyAndDelete_flow() throws Exception {
        MockHttpSession session = login("codex_message_sender");

        Long messageId = sendMessage(
                session,
                senderId,
                receiverId,
                "codex_message_api_title",
                "codex message api content"
        );

        mockMvc.perform(get("/api/messages/received")
                        .session(session)
                        .param("userId", receiverId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].messageId").value(messageId))
                .andExpect(jsonPath("$.content[0].title").value("codex_message_api_title"))
                .andExpect(jsonPath("$.content[0].readYn").value("N"));

        mockMvc.perform(get("/api/messages/{messageId}", messageId)
                        .session(session)
                        .param("userId", receiverId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(messageId))
                .andExpect(jsonPath("$.sendId").value(senderId))
                .andExpect(jsonPath("$.receivedId").value(receiverId))
                .andExpect(jsonPath("$.readYn").value("Y"));

        mockMvc.perform(get("/api/messages/sent")
                        .session(session)
                        .param("userId", senderId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].messageId").value(messageId))
                .andExpect(jsonPath("$.content[0].readYn").value("Y"));

        mockMvc.perform(post("/api/messages/{messageId}/reply", messageId)
                        .session(session)
                        .param("userId", receiverId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_message_api_reply",
                                  "content": "codex message api reply content"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNumber());

        mockMvc.perform(delete("/api/messages/{messageId}/received", messageId)
                        .session(session)
                        .param("userId", receiverId.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/messages/received")
                        .session(session)
                        .param("userId", receiverId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()));

        mockMvc.perform(delete("/api/messages/{messageId}/sent", messageId)
                        .session(session)
                        .param("userId", senderId.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/messages/sent")
                        .session(session)
                        .param("userId", senderId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()));

        mockMvc.perform(get("/api/messages/received")
                        .session(session)
                        .param("userId", senderId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("codex_message_api_reply"));
    }

    @Test
    void getReceivedMessages_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/messages/received")
                        .param("userId", receiverId.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendMessage_rejectsSelfMessage() throws Exception {
        MockHttpSession session = login("codex_message_sender");

        mockMvc.perform(post("/api/messages")
                        .session(session)
                        .param("senderId", senderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiveId": %d,
                                  "title": "codex_message_api_self",
                                  "content": "self message"
                                }
                                """.formatted(senderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신에게는 쪽지를 보낼 수 없습니다."));
    }

    @Test
    void getMessage_rejectsUserWhoIsNotSenderOrReceiver() throws Exception {
        MockHttpSession session = login("codex_message_sender");
        Long messageId = sendMessage(
                session,
                senderId,
                receiverId,
                "codex_message_api_private",
                "private message"
        );

        mockMvc.perform(get("/api/messages/{messageId}", messageId)
                        .session(session)
                        .param("userId", otherUserId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 쪽지를 조회할 권한이 없습니다."));
    }

    @Test
    void sendMessage_rejectsMissingReceiver() throws Exception {
        MockHttpSession session = login("codex_message_sender");

        mockMvc.perform(post("/api/messages")
                        .session(session)
                        .param("senderId", senderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_message_api_no_receiver",
                                  "content": "missing receiver"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("수신자는 필수입니다"));
    }

    private Long sendMessage(
            MockHttpSession session,
            Long senderId,
            Long receiverId,
            String title,
            String content
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/messages")
                        .session(session)
                        .param("senderId", senderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiveId": %d,
                                  "title": "%s",
                                  "content": "%s"
                                }
                                """.formatted(receiverId, title, content)))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString());
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
                        delete from messages
                        where sender_id in (
                            select user_id
                            from users
                            where login_id like 'codex_message_%'
                        )
                        or receiver_id in (
                            select user_id
                            from users
                            where login_id like 'codex_message_%'
                        )
                        """
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_message_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id like 'codex_message_%'"
        );
    }
}
