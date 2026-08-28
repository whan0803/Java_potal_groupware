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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long roleIdA;
    private Long roleIdB;

    @BeforeEach
    void setUp() {
        cleanUp();
        userId = createUser();
        roleIdA = createRole("ROLE_CODEX_USER_A", "Codex User A");
        roleIdB = createRole("ROLE_CODEX_USER_B", "Codex User B");
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void user_updateBasicInfoAndMultipleRoles_flow() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userName": "Codex User Updated",
                                  "email": "codex_user_updated@example.com",
                                  "phone": "010-1234-5678",
                                  "useYn": "Y",
                                  "roleIds": [%d, %d]
                                }
                                """.formatted(roleIdA, roleIdB)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{userId}", userId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Codex User Updated"))
                .andExpect(jsonPath("$.email").value("codex_user_updated@example.com"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles[*].roleCode", containsInAnyOrder(
                        "ROLE_CODEX_USER_A",
                        "ROLE_CODEX_USER_B"
                )));
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "codex_user_api",
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
                            'codex_user_api',
                            ?,
                            'Codex User',
                            'codex_user_api@example.com',
                            'Y',
                            now()
                        )
                        returning user_id
                        """,
                Long.class,
                PASSWORD_HASH
        );
    }

    private Long createRole(String roleCode, String roleName) {
        return jdbcTemplate.queryForObject(
                """
                        insert into roles (
                            role_code,
                            role_name,
                            role_description,
                            use_yn,
                            created_at
                        )
                        values (?, ?, 'codex user api role', 'Y', now())
                        returning role_id
                        """,
                Long.class,
                roleCode,
                roleName
        );
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id = 'codex_user_api'
                        )
                        or role_id in (
                            select role_id
                            from roles
                            where role_code like 'ROLE_CODEX_USER_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id = 'codex_user_api'"
        );
        jdbcTemplate.update(
                "delete from roles where role_code like 'ROLE_CODEX_USER_%'"
        );
    }
}
