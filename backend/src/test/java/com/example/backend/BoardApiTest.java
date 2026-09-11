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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BoardApiTest {

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
    void deleteBoard_removesHiddenInactivePostsFirst() throws Exception {
        MockHttpSession session = login();
        Long boardId = createBoard();
        createInactivePost(boardId);

        mockMvc.perform(delete("/api/boards/{boardId}", boardId)
                        .session(session))
                .andExpect(status().isNoContent());

        assertEquals(0, count("select count(*) from posts where board_id = ?", boardId));
        assertEquals(0, count("select count(*) from boards where board_id = ?", boardId));
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "codex_board_delete_user",
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
                            login_id, password, user_name, email, use_yn, created_at
                        ) values (
                            'codex_board_delete_user', ?, 'Codex Board User',
                            'codex_board_delete_user@example.com', 'Y', now()
                        ) returning user_id
                        """,
                Long.class,
                PASSWORD_HASH
        );
    }

    private Long createBoard() {
        return jdbcTemplate.queryForObject(
                """
                        insert into boards (
                            board_name, board_description, attachment_yn, use_yn,
                            created_at, created_by
                        ) values (
                            'codex_board_delete_target', 'delete test', 'N', 'Y', now(), ?
                        ) returning board_id
                        """,
                Long.class,
                userId
        );
    }

    private void createInactivePost(Long boardId) {
        jdbcTemplate.update(
                """
                        insert into posts (
                            board_id, title, content, writer_id, view_count, use_yn,
                            created_at, created_by
                        ) values (?, 'codex hidden post', 'hidden', ?, 0, 'N', now(), ?)
                        """,
                boardId,
                userId,
                userId
        );
    }

    private Integer count(String sql, Long id) {
        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from posts
                        where board_id in (
                            select board_id from boards
                            where board_name = 'codex_board_delete_target'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from boards where board_name = 'codex_board_delete_target'"
        );
        jdbcTemplate.update(
                "delete from users where login_id = 'codex_board_delete_user'"
        );
    }
}
