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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostReactionApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long boardId;
    private Long postId;

    @BeforeEach
    void setUp() {
        cleanUp();
        userId = createUser();
        boardId = createBoard();
        postId = createPost();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void reactionIsUniquePerUserAndRestoredWhenPostIsOpenedAgain() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/posts/{postId}/recommend", postId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendCount").value(1))
                .andExpect(jsonPath("$.dislikeCount").value(0))
                .andExpect(jsonPath("$.myReaction").value("recommend"));

        mockMvc.perform(patch("/api/posts/{postId}/recommend", postId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendCount").value(1))
                .andExpect(jsonPath("$.myReaction").value("recommend"));

        assertEquals(1, count(
                "select count(*) from post_reactions where post_id = ? and user_id = ?",
                postId,
                userId
        ));

        mockMvc.perform(get("/api/posts/{postId}", postId)
                        .param("increaseView", "false")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myReaction").value("recommend"));

        mockMvc.perform(patch("/api/posts/{postId}/dislike", postId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendCount").value(0))
                .andExpect(jsonPath("$.dislikeCount").value(1))
                .andExpect(jsonPath("$.myReaction").value("dislike"));

        mockMvc.perform(patch("/api/posts/{postId}/dislike/cancel", postId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendCount").value(0))
                .andExpect(jsonPath("$.dislikeCount").value(0))
                .andExpect(jsonPath("$.myReaction").value(nullValue()));
    }

    @Test
    void ownerCanDeletePostUsingAuthenticatedSession() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/posts/{postId}/delete", postId).session(session))
                .andExpect(status().isNoContent());

        assertEquals("N", jdbcTemplate.queryForObject(
                "select trim(use_yn) from posts where post_id = ?",
                String.class,
                postId
        ));
        assertEquals(userId, jdbcTemplate.queryForObject(
                "select updated_by from posts where post_id = ?",
                Long.class,
                postId
        ));
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "codex_post_reaction_user",
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
                            'codex_post_reaction_user', ?, 'Reaction Test User',
                            'codex_post_reaction_user@example.com', 'Y', now()
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
                            'codex_post_reaction_board', 'reaction test', 'N', 'Y', now(), ?
                        ) returning board_id
                        """,
                Long.class,
                userId
        );
    }

    private Long createPost() {
        return jdbcTemplate.queryForObject(
                """
                        insert into posts (
                            board_id, title, content, writer_id, view_count,
                            recommend_count, dislike_count, use_yn, created_at, created_by
                        ) values (?, 'codex reaction post', 'reaction test', ?, 0, 0, 0, 'Y', now(), ?)
                        returning post_id
                        """,
                Long.class,
                boardId,
                userId,
                userId
        );
    }

    private Integer count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from post_reactions
                        where user_id in (
                            select user_id from users where login_id = 'codex_post_reaction_user'
                        )
                        """
        );
        jdbcTemplate.update("delete from posts where title = 'codex reaction post'");
        jdbcTemplate.update("delete from boards where board_name = 'codex_post_reaction_board'");
        jdbcTemplate.update("delete from users where login_id = 'codex_post_reaction_user'");
    }
}
