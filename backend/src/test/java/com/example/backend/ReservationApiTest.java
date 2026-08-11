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
class ReservationApiTest {

    private static final String PASSWORD_HASH =
            "$2a$10$iWYoJPvZ/ewzsFv9tfnrH.kJlFVZXI3mvv/WH1bPbuuNdOXQmckQG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long requesterId;
    private Long resourceId;

    @BeforeEach
    void setUp() {
        cleanUp();
        requesterId = createUser();
        resourceId = createResource();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void createListDetailUpdateCancelAndRebook_flow() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/reservations/resources")
                        .session(session)
                        .param("type", "MEETING_ROOM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resourceId").value(resourceId))
                .andExpect(jsonPath("$[0].resourceName").value("codex_reservation_room"));

        Long reservationId = createReservation(
                session,
                "codex_reservation_title",
                "codex reservation purpose",
                "2099-08-10T10:00:00",
                "2099-08-10T11:00:00"
        );

        mockMvc.perform(get("/api/reservations")
                        .session(session)
                        .param("resourceId", resourceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reservationId").value(reservationId))
                .andExpect(jsonPath("$[0].status").value("REQUESTED"));

        mockMvc.perform(get("/api/reservations/{id}", reservationId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.resourceId").value(resourceId))
                .andExpect(jsonPath("$.requesterId").value(requesterId))
                .andExpect(jsonPath("$.title").value("codex_reservation_title"));

        mockMvc.perform(put("/api/reservations/{id}", reservationId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_reservation_title_updated",
                                  "purpose": "codex reservation purpose updated",
                                  "startDateTime": "2099-08-10T12:00:00",
                                  "endDateTime": "2099-08-10T13:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/{id}", reservationId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("codex_reservation_title_updated"))
                .andExpect(jsonPath("$.startDatetime").value("2099-08-10T12:00:00"))
                .andExpect(jsonPath("$.endDatetime").value("2099-08-10T13:00:00"));

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/{id}", reservationId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        createReservation(
                session,
                "codex_reservation_rebook",
                "codex reservation rebook",
                "2099-08-10T12:00:00",
                "2099-08-10T13:00:00"
        );

        mockMvc.perform(get("/api/reservations")
                        .session(session)
                        .param("resourceId", resourceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void createReservation_rejectsOverlap() throws Exception {
        MockHttpSession session = login();

        createReservation(
                session,
                "codex_reservation_overlap_base",
                "base",
                "2099-08-10T10:00:00",
                "2099-08-10T11:00:00"
        );

        mockMvc.perform(post("/api/reservations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "requesterId": %d,
                                  "title": "codex_reservation_overlap",
                                  "purpose": "overlap",
                                  "startDateTime": "2099-08-10T10:30:00",
                                  "endDateTime": "2099-08-10T11:30:00"
                                }
                                """.formatted(resourceId, requesterId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다."));
    }

    @Test
    void updateReservation_rejectsOverlapWithAnotherReservation() throws Exception {
        MockHttpSession session = login();

        Long firstReservationId = createReservation(
                session,
                "codex_reservation_first",
                "first",
                "2099-08-10T10:00:00",
                "2099-08-10T11:00:00"
        );

        createReservation(
                session,
                "codex_reservation_second",
                "second",
                "2099-08-10T12:00:00",
                "2099-08-10T13:00:00"
        );

        mockMvc.perform(put("/api/reservations/{id}", firstReservationId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "codex_reservation_first_updated",
                                  "purpose": "overlap update",
                                  "startDateTime": "2099-08-10T12:30:00",
                                  "endDateTime": "2099-08-10T13:30:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다."));
    }

    @Test
    void createReservation_rejectsInvalidPeriod() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/api/reservations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "requesterId": %d,
                                  "title": "codex_reservation_invalid_period",
                                  "purpose": "invalid",
                                  "startDateTime": "2099-08-10T11:00:00",
                                  "endDateTime": "2099-08-10T10:00:00"
                                }
                                """.formatted(resourceId, requesterId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("종료 시간은 시작 시간보다 늦어야 합니다."));
    }

    @Test
    void cancelReservation_rejectsAlreadyCanceled() throws Exception {
        MockHttpSession session = login();

        Long reservationId = createReservation(
                session,
                "codex_reservation_cancel",
                "cancel",
                "2099-08-10T10:00:00",
                "2099-08-10T11:00:00"
        );

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId)
                        .session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 취소된 예약입니다."));
    }

    @Test
    void reservations_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/reservations/resources")
                        .param("type", "MEETING_ROOM"))
                .andExpect(status().isUnauthorized());
    }

    private Long createReservation(
            MockHttpSession session,
            String title,
            String purpose,
            String startDateTime,
            String endDateTime
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "requesterId": %d,
                                  "title": "%s",
                                  "purpose": "%s",
                                  "startDateTime": "%s",
                                  "endDateTime": "%s"
                                }
                                """.formatted(
                                resourceId,
                                requesterId,
                                title,
                                purpose,
                                startDateTime,
                                endDateTime
                        )))
                .andExpect(status().isOk())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString());
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "codex_reservation_user",
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
                            'codex_reservation_user',
                            ?,
                            'Codex Reservation User',
                            'codex_reservation_user@example.com',
                            'Y',
                            now()
                        )
                        returning user_id
                        """,
                Long.class,
                PASSWORD_HASH
        );
    }

    private Long createResource() {
        return jdbcTemplate.queryForObject(
                """
                        insert into reservation_resources (
                            resource_type,
                            resource_name,
                            resource_description,
                            capacity,
                            location,
                            use_yn,
                            created_at
                        )
                        values (
                            'MEETING_ROOM',
                            'codex_reservation_room',
                            'codex reservation test room',
                            8,
                            'Codex Floor',
                            'Y',
                            now()
                        )
                        returning resource_id
                        """,
                Long.class
        );
    }

    private void cleanUp() {
        jdbcTemplate.update(
                """
                        delete from reservations
                        where resource_id in (
                            select resource_id
                            from reservation_resources
                            where resource_name like 'codex_reservation_%'
                        )
                        or requester_id in (
                            select user_id
                            from users
                            where login_id like 'codex_reservation_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from reservation_resources where resource_name like 'codex_reservation_%'"
        );
        jdbcTemplate.update(
                """
                        delete from user_roles
                        where user_id in (
                            select user_id
                            from users
                            where login_id like 'codex_reservation_%'
                        )
                        """
        );
        jdbcTemplate.update(
                "delete from users where login_id like 'codex_reservation_%'"
        );
    }
}
