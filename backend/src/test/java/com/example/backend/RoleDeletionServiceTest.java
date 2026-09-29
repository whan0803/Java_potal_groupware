package com.example.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import role.service.RoleService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RoleDeletionServiceTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private RoleService roleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long roleId;

    @BeforeEach
    void setUp() {
        cleanUp();
        userId = jdbcTemplate.queryForObject(
                """
                        insert into users (
                            login_id, password, user_name, email, use_yn, created_at
                        ) values (
                            'codex_role_delete_user', ?, 'Role Delete User',
                            'codex_role_delete_user@example.com', 'Y', now()
                        ) returning user_id
                        """,
                Long.class,
                PASSWORD_HASH
        );
        roleId = jdbcTemplate.queryForObject(
                """
                        insert into roles (
                            role_code, role_name, role_description, use_yn, created_at, created_by
                        ) values (
                            'ROLE_CODEX_DELETE_TARGET', 'Codex Delete Target',
                            'role deletion test', 'Y', now(), ?
                        ) returning role_id
                        """,
                Long.class,
                userId
        );
        jdbcTemplate.update(
                "insert into user_roles (user_id, role_id, created_at, created_by) values (?, ?, now(), ?)",
                userId,
                roleId,
                userId
        );
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void deleteRoleRemovesUserAssignmentsBeforeRole() {
        assertEquals(1L, roleService.getRole(roleId).userCount());

        roleService.deleteRole(roleId);

        assertEquals(0, count("select count(*) from user_roles where role_id = ?", roleId));
        assertEquals(0, count("select count(*) from roles where role_id = ?", roleId));
    }

    private Integer count(String sql, Long id) {
        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from role_menus
                        where role_id in (
                            select role_id from roles where role_code = 'ROLE_CODEX_DELETE_TARGET'
                        )
                        """
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where role_id in (
                            select role_id from roles where role_code = 'ROLE_CODEX_DELETE_TARGET'
                        )
                           or user_id in (
                            select user_id from users where login_id = 'codex_role_delete_user'
                        )
                        """
        );
        jdbcTemplate.update("delete from roles where role_code = 'ROLE_CODEX_DELETE_TARGET'");
        jdbcTemplate.update("delete from users where login_id = 'codex_role_delete_user'");
    }
}
