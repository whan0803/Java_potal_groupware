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

import java.time.LocalDate;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long adminId;
    private Long userId;
    private Long approverId;
    private Long roleId;

    @BeforeEach
    void setUp() {
        cleanUp();
        adminId = createUser(
                "codex_dashboard_admin",
                "Codex Dashboard Admin"
        );
        userId = createUser(
                "codex_dashboard_user",
                "Codex Dashboard User"
        );
        approverId = createUser(
                "codex_dashboard_approver",
                "Codex Dashboard Approver"
        );
        roleId = createAdminRole();
        assignRole(adminId, roleId);
        createDashboardData();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void dashboard_returnsUserSpecificItems() throws Exception {
        MockHttpSession session =
                login("codex_dashboard_approver");

        mockMvc.perform(get("/api/dashboard")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalUserCount", nullValue()))
                .andExpect(jsonPath("$.summary.activeUserCount", nullValue()))
                .andExpect(jsonPath("$.summary.pendingReservationCount", nullValue()))
                .andExpect(jsonPath("$.summary.pendingApprovalCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.summary.inProgressTaskCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.summary.monthlyScheduleCount", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.summary.unreadMessageCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.notices[?(@.title == 'codex_dashboard_notice')]").exists())
                .andExpect(jsonPath("$.pendingApprovals[?(@.title == 'codex_dashboard_approval')]").exists())
                .andExpect(jsonPath("$.pendingReservations", empty()))
                .andExpect(jsonPath("$.inProgressTasks[?(@.title == 'codex_dashboard_task')]").exists())
                .andExpect(jsonPath("$.todaySchedules[?(@.title == 'codex_dashboard_personal_schedule')]").exists())
                .andExpect(jsonPath("$.todaySchedules[?(@.title == 'codex_dashboard_public_schedule')]").exists())
                .andExpect(jsonPath("$.receivedMessages[?(@.title == 'codex_dashboard_message')]").exists())
                .andExpect(jsonPath("$.recentChanges", empty()));
    }

    @Test
    void dashboard_returnsAdminOnlyItemsForAdmin() throws Exception {
        MockHttpSession session =
                login("codex_dashboard_admin");

        mockMvc.perform(get("/api/dashboard")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalUserCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.summary.activeUserCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.summary.pendingReservationCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.pendingReservations[?(@.resourceName == 'codex_dashboard_room')]").exists())
                .andExpect(jsonPath("$.recentChanges", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.recentChanges[?(@.targetName == 'dashboard_test')]").exists());
    }

    @Test
    void dashboard_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    private void createDashboardData() {
        LocalDate today =
                LocalDate.now();

        jdbcTemplate.update(
                """
                        insert into notices (
                            title,
                            content,
                            writer_id,
                            start_date,
                            end_date,
                            important_yn,
                            view_count,
                            use_yn,
                            created_at,
                            created_by
                        )
                        values (
                            'codex_dashboard_notice',
                            'dashboard notice',
                            ?,
                            ?,
                            ?,
                            'Y',
                            0,
                            'Y',
                            now(),
                            ?
                        )
                        """,
                adminId,
                today.minusDays(1),
                today.plusDays(30),
                adminId
        );

        Long approvalDocumentId =
                jdbcTemplate.queryForObject(
                        """
                                insert into approval_documents (
                                    document_number,
                                    drafter_id,
                                    title,
                                    content,
                                    approval_status,
                                    use_yn,
                                    created_at,
                                    created_by
                                )
                                values (
                                    'CODEX-DSH-APPROVAL',
                                    ?,
                                    'codex_dashboard_approval',
                                    'dashboard approval',
                                    'IN_PROGRESS',
                                    'Y',
                                    now(),
                                    ?
                                )
                                returning approval_document_id
                                """,
                        Long.class,
                        userId,
                        userId
                );

        jdbcTemplate.update(
                """
                        insert into approval_lines (
                            approval_document_id,
                            approver_id,
                            approval_order,
                            approval_type,
                            approval_status,
                            created_at,
                            created_by
                        )
                        values (?, ?, 1, 'APPROVAL', 'PENDING', now(), ?)
                        """,
                approvalDocumentId,
                approverId,
                userId
        );

        Long resourceId =
                jdbcTemplate.queryForObject(
                        """
                                insert into reservation_resources (
                                    resource_type,
                                    resource_name,
                                    resource_description,
                                    capacity,
                                    location,
                                    use_yn,
                                    created_at,
                                    created_by
                                )
                                values (
                                    'MEETING_ROOM',
                                    'codex_dashboard_room',
                                    'dashboard room',
                                    8,
                                    'Dashboard Floor',
                                    'Y',
                                    now(),
                                    ?
                                )
                                returning resource_id
                                """,
                        Long.class,
                        adminId
                );

        jdbcTemplate.update(
                """
                        insert into reservations (
                            resource_id,
                            requester_id,
                            title,
                            purpose,
                            start_datetime,
                            end_datetime,
                            reservation_status,
                            use_yn,
                            created_at,
                            created_by
                        )
                        values (
                            ?,
                            ?,
                            'codex_dashboard_reservation',
                            'dashboard reservation',
                            ?,
                            ?,
                            'REQUESTED',
                            'Y',
                            now(),
                            ?
                        )
                        """,
                resourceId,
                userId,
                today.atTime(10, 0),
                today.atTime(11, 0),
                userId
        );

        jdbcTemplate.update(
                """
                        insert into tasks (
                            requester_id,
                            assignee_id,
                            title,
                            content,
                            task_status,
                            priority,
                            start_date,
                            due_date,
                            progress_rate,
                            use_yn,
                            created_at,
                            created_by
                        )
                        values (
                            ?,
                            ?,
                            'codex_dashboard_task',
                            'dashboard task',
                            'IN_PROGRESS',
                            'NORMAL',
                            ?,
                            ?,
                            40,
                            'Y',
                            now(),
                            ?
                        )
                        """,
                userId,
                approverId,
                today,
                today.plusDays(7),
                userId
        );

        jdbcTemplate.update(
                """
                        insert into schedules (
                            user_id,
                            title,
                            content,
                            location,
                            schedule_type,
                            start_datetime,
                            end_datetime,
                            all_day_yn,
                            use_yn,
                            created_at,
                            created_by
                        )
                        values (
                            ?,
                            'codex_dashboard_personal_schedule',
                            'dashboard schedule',
                            'Dashboard Room',
                            'PERSONAL',
                            ?,
                            ?,
                            'N',
                            'Y',
                            now(),
                            ?
                        )
                        """,
                approverId,
                today.atTime(13, 0),
                today.atTime(14, 0),
                approverId
        );

        jdbcTemplate.update(
                """
                        insert into schedules (
                            user_id,
                            title,
                            content,
                            location,
                            schedule_type,
                            start_datetime,
                            end_datetime,
                            all_day_yn,
                            use_yn,
                            created_at,
                            created_by
                        )
                        values (
                            ?,
                            'codex_dashboard_public_schedule',
                            'dashboard public schedule',
                            'Dashboard Hall',
                            'PUBLIC',
                            ?,
                            ?,
                            'N',
                            'Y',
                            now(),
                            ?
                        )
                        """,
                adminId,
                today.atTime(15, 0),
                today.atTime(16, 0),
                adminId
        );

        jdbcTemplate.update(
                """
                        insert into messages (
                            sender_id,
                            receiver_id,
                            title,
                            content,
                            read_yn,
                            sender_delete_yn,
                            receiver_delete_yn,
                            created_at
                        )
                        values (
                            ?,
                            ?,
                            'codex_dashboard_message',
                            'dashboard message',
                            'N',
                            'N',
                            'N',
                            now()
                        )
                        """,
                userId,
                approverId
        );

        jdbcTemplate.update(
                """
                        insert into audit_logs (
                            table_name,
                            record_id,
                            action_type,
                            actor_id,
                            created_at
                        )
                        values (
                            'dashboard_test',
                            1,
                            'INSERT',
                            ?,
                            now()
                        )
                        """,
                adminId
        );
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
                        values (?, ?, ?, concat(?, '@example.com'), 'Y', now())
                        returning user_id
                        """,
                Long.class,
                loginId,
                PASSWORD_HASH,
                userName,
                loginId
        );
    }

    private Long createAdminRole() {
        return jdbcTemplate.queryForObject(
                """
                        insert into roles (
                            role_code,
                            role_name,
                            role_description,
                            use_yn,
                            created_at
                        )
                        values (
                            'ADMIN',
                            '관리자',
                            'codex dashboard api test role',
                            'Y',
                            now()
                        )
                        on conflict (role_code)
                        do update set use_yn = 'Y'
                        returning role_id
                        """,
                Long.class
        );
    }

    private void assignRole(
            Long userId,
            Long roleId
    ) {
        jdbcTemplate.update(
                """
                        insert into user_roles (
                            user_id,
                            role_id,
                            created_at,
                            created_by
                        )
                        values (?, ?, now(), ?)
                        on conflict (user_id, role_id)
                        do nothing
                        """,
                userId,
                roleId,
                userId
        );
    }

    private void cleanUp() {
        jdbcTemplate.update(
                "delete from audit_logs where table_name = 'dashboard_test'"
        );
        jdbcTemplate.update(
                "delete from messages where title like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                "delete from schedules where title like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                "delete from tasks where title like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                "delete from reservations where title like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                "delete from reservation_resources where resource_name like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                """
                        delete from approval_lines
                        where approval_document_id in (
                            select approval_document_id
                            from approval_documents
                            where document_number = 'CODEX-DSH-APPROVAL'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from approval_documents where document_number = 'CODEX-DSH-APPROVAL'"
        );
        jdbcTemplate.update(
                "delete from notices where title like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_dashboard_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id like 'codex_dashboard_%'"
        );
        jdbcTemplate.update(
                """
                        delete from roles
                        where role_code = 'ADMIN'
                          and role_description = 'codex dashboard api test role'
                        """
        );
    }
}
