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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        cleanUp();
        ownerId = createUser(
                "codex_schedule_owner",
                "codex_schedule_owner@example.com",
                "Codex Schedule Owner"
        );
        otherUserId = createUser(
                "codex_schedule_other",
                "codex_schedule_other@example.com",
                "Codex Schedule Other"
        );
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void createListDetailUpdateAndDelete_flow() throws Exception {
        MockHttpSession session = login("codex_schedule_owner");

        Long scheduleId = createSchedule(
                session,
                "codex_schedule_api_title",
                "codex schedule api content",
                "PERSONAL",
                "2099-08-09T10:00:00",
                "2099-08-09T11:00:00",
                "N"
        );

        mockMvc.perform(get("/api/schedules/monthly")
                        .session(session)
                        .param("start", "2099-08-01T00:00:00")
                        .param("end", "2099-08-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].scheduleId").value(scheduleId))
                .andExpect(jsonPath("$[0].title").value("codex_schedule_api_title"));

        mockMvc.perform(get("/api/schedules/daily")
                        .session(session)
                        .param("start", "2099-08-09T00:00:00")
                        .param("end", "2099-08-09T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].scheduleId").value(scheduleId));

        mockMvc.perform(get("/api/schedules/{scheduleId}", scheduleId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId))
                .andExpect(jsonPath("$.userId").value(ownerId))
                .andExpect(jsonPath("$.title").value("codex_schedule_api_title"))
                .andExpect(jsonPath("$.allDayYn").value("N"));

        mockMvc.perform(put("/api/schedules/{scheduleId}", scheduleId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_schedule_api_title_updated",
                                  "content": "codex schedule api content updated",
                                  "location": "Codex Room 2",
                                  "startDatetime": "2099-08-09T12:00:00",
                                  "endDatetime": "2099-08-09T13:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/schedules/{scheduleId}", scheduleId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("codex_schedule_api_title_updated"))
                .andExpect(jsonPath("$.location").value("Codex Room 2"))
                .andExpect(jsonPath("$.startDatetime").value("2099-08-09T12:00:00"));

        mockMvc.perform(delete("/api/schedules/{scheduleId}", scheduleId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/schedules/monthly")
                        .session(session)
                        .param("start", "2099-08-01T00:00:00")
                        .param("end", "2099-08-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));

        mockMvc.perform(get("/api/schedules/{scheduleId}", scheduleId)
                        .session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("삭제되었거나 사용 중지된 일정입니다"));
    }

    @Test
    void monthly_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/schedules/monthly")
                        .param("start", "2099-08-01T00:00:00")
                        .param("end", "2099-08-31T23:59:59"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSchedule_rejectsInvalidPeriod() throws Exception {
        MockHttpSession session = login("codex_schedule_owner");

        mockMvc.perform(post("/api/schedules")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_schedule_api_invalid_period",
                                  "content": "invalid period",
                                  "location": "Codex Room",
                                  "scheduleType": "PERSONAL",
                                  "startDatetime": "2099-08-09T11:00:00",
                                  "endDatetime": "2099-08-09T10:00:00",
                                  "allDayYn": "N"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("종료 시간은 시작 시간보다 빠를 수 없습니다"));
    }

    @Test
    void createSchedule_rejectsInvalidTypeAndAllDayYn() throws Exception {
        MockHttpSession session = login("codex_schedule_owner");

        mockMvc.perform(post("/api/schedules")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_schedule_api_invalid_values",
                                  "content": "invalid values",
                                  "location": "Codex Room",
                                  "scheduleType": "TEAM",
                                  "startDatetime": "2099-08-09T10:00:00",
                                  "endDatetime": "2099-08-09T11:00:00",
                                  "allDayYn": "X"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSchedule_rejectsNonOwner() throws Exception {
        MockHttpSession ownerSession = login("codex_schedule_owner");
        MockHttpSession otherSession = login("codex_schedule_other");
        Long scheduleId = createSchedule(
                ownerSession,
                "codex_schedule_api_private",
                "private schedule",
                "PERSONAL",
                "2099-08-09T10:00:00",
                "2099-08-09T11:00:00",
                "N"
        );

        mockMvc.perform(put("/api/schedules/{scheduleId}", scheduleId)
                        .session(otherSession)
                        .param("userId", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_schedule_api_hijack",
                                  "content": "hijack schedule",
                                  "location": "Other Room",
                                  "startDatetime": "2099-08-09T12:00:00",
                                  "endDatetime": "2099-08-09T13:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("작성자만 수정 가능합니다"));
    }

    @Test
    void personalSchedule_isHiddenFromOtherUsersButPublicScheduleIsVisible() throws Exception {
        MockHttpSession ownerSession = login("codex_schedule_owner");
        MockHttpSession otherSession = login("codex_schedule_other");

        Long personalScheduleId = createSchedule(
                ownerSession,
                "codex_schedule_api_personal",
                "personal schedule",
                "PERSONAL",
                "2099-08-09T10:00:00",
                "2099-08-09T11:00:00",
                "N"
        );

        Long publicScheduleId = createSchedule(
                ownerSession,
                "codex_schedule_api_public",
                "public schedule",
                "PUBLIC",
                "2099-08-09T12:00:00",
                "2099-08-09T13:00:00",
                "N"
        );

        mockMvc.perform(get("/api/schedules/monthly")
                        .session(otherSession)
                        .param("start", "2099-08-01T00:00:00")
                        .param("end", "2099-08-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].scheduleId").value(publicScheduleId))
                .andExpect(jsonPath("$[0].title").value("codex_schedule_api_public"));

        mockMvc.perform(get("/api/schedules/{scheduleId}", personalScheduleId)
                        .session(otherSession))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("일정을 조회할 권한이 없습니다"));

        mockMvc.perform(get("/api/schedules/{scheduleId}", publicScheduleId)
                        .session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(publicScheduleId));
    }

    private Long createSchedule(
            MockHttpSession session,
            String title,
            String content,
            String scheduleType,
            String startDatetime,
            String endDatetime,
            String allDayYn
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/schedules")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "content": "%s",
                                  "location": "Codex Room",
                                  "scheduleType": "%s",
                                  "startDatetime": "%s",
                                  "endDatetime": "%s",
                                  "allDayYn": "%s"
                                }
                                """.formatted(
                                title,
                                content,
                                scheduleType,
                                startDatetime,
                                endDatetime,
                                allDayYn
                        )))
                .andExpect(status().isOk())
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
                        delete from schedules
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_schedule_%'
                        )
                        """
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_schedule_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id like 'codex_schedule_%'"
        );
    }
}
