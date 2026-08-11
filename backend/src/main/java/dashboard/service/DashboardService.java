package dashboard.service;

import approval.entity.ApprovalLine;
import approval.repository.ApprovalLineRepository;
import dashboard.dto.ApprovalDashboardResponse;
import dashboard.dto.ChangeHistoryDashboardResponse;
import dashboard.dto.DashboardResponse;
import dashboard.dto.DashboardSummaryResponse;
import dashboard.dto.MessageDashboardResponse;
import dashboard.dto.NoticeDashboardResponse;
import dashboard.dto.ReservationDashboardResponse;
import dashboard.dto.ScheduleDashboardResponse;
import dashboard.dto.TaskDashboardResponse;
import lombok.RequiredArgsConstructor;
import message.repository.MessageRepository;
import notice.repository.NoticeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reservation.repository.ReservationRepository;
import schedule.repository.ScheduleRepository;
import security.CustomUserDetails;
import task.repository.TaskRepository;
import user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int DASHBOARD_LIST_SIZE = 5;

    private final UserRepository userRepository;
    private final ApprovalLineRepository approvalLineRepository;
    private final ReservationRepository reservationRepository;
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;
    private final MessageRepository messageRepository;
    private final NoticeRepository noticeRepository;
    private final JdbcTemplate jdbcTemplate;

    public DashboardResponse getDashboard(
            Authentication authentication
    ) {
        CustomUserDetails userDetails =
                getCurrentUser(authentication);

        Long userId = userDetails.getUserId();
        boolean admin = hasAdminRole(authentication);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart =
                today.atStartOfDay();
        LocalDateTime todayEnd =
                today.atTime(LocalTime.MAX);
        LocalDateTime monthStart =
                today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd =
                today.withDayOfMonth(today.lengthOfMonth())
                        .atTime(LocalTime.MAX);

        Long totalUserCount =
                admin ? userRepository.count() : null;
        Long activeUserCount =
                admin ? userRepository.countByUseYn("Y") : null;

        Long pendingApprovalCount =
                approvalLineRepository
                        .countByApproverUserIdAndApprovalStatus(
                                userId,
                                "PENDING"
                        );

        Long pendingReservationCount =
                admin
                        ? reservationRepository
                        .countByReservationStatusAndUseYn(
                                "REQUESTED",
                                "Y"
                        )
                        : null;

        Long inProgressTaskCount =
                taskRepository
                        .countByAssigneeUserIdAndTaskStatusAndUseYn(
                                userId,
                                "IN_PROGRESS",
                                "Y"
                        );

        Long monthlyScheduleCount =
                (long) scheduleRepository
                        .findVisibleSchedules(
                                monthStart,
                                monthEnd,
                                userId
                        )
                        .size();

        Long unreadMessageCount =
                messageRepository
                        .countByReceiverUserIdAndReadYnAndReceiverDeleteYn(
                                userId,
                                "N",
                                "N"
                        );

        DashboardSummaryResponse summary =
                new DashboardSummaryResponse(
                        totalUserCount,
                        activeUserCount,
                        pendingApprovalCount,
                        pendingReservationCount,
                        inProgressTaskCount,
                        monthlyScheduleCount,
                        unreadMessageCount
                );

        PageRequest listLimit =
                PageRequest.of(
                        0,
                        DASHBOARD_LIST_SIZE
                );

        return new DashboardResponse(
                summary,
                getImportantNotices(today, listLimit),
                getPendingApprovals(userId, listLimit),
                admin ? getPendingReservations(listLimit) : List.of(),
                getInProgressTasks(userId, listLimit),
                getTodaySchedules(userId, todayStart, todayEnd),
                getReceivedMessages(userId, listLimit),
                admin ? getRecentChanges(listLimit) : List.of()
        );
    }

    private List<NoticeDashboardResponse> getImportantNotices(
            LocalDate today,
            PageRequest listLimit
    ) {
        return noticeRepository
                .findImportantVisibleNotices(today, listLimit)
                .stream()
                .map(notice ->
                        new NoticeDashboardResponse(
                                notice.getNoticeId(),
                                notice.getTitle(),
                                notice.getWriter().getUserName(),
                                notice.getCreatedAt()
                        )
                )
                .toList();
    }

    private List<ApprovalDashboardResponse> getPendingApprovals(
            Long userId,
            PageRequest listLimit
    ) {
        return approvalLineRepository
                .findByApproverUserIdAndApprovalStatusOrderByApprovalDocumentCreatedAtDesc(
                        userId,
                        "PENDING",
                        listLimit
                )
                .stream()
                .map(line ->
                        new ApprovalDashboardResponse(
                                line.getApprovalDocument()
                                        .getApprovalDocumentId(),
                                line.getApprovalDocument()
                                        .getTitle(),
                                line.getApprovalDocument()
                                        .getDrafter()
                                        .getUserName(),
                                line.getApprovalDocument()
                                        .getCreatedAt()
                        )
                )
                .toList();
    }

    private List<ReservationDashboardResponse> getPendingReservations(
            PageRequest listLimit
    ) {
        return reservationRepository
                .findByReservationStatusAndUseYnOrderByStartDatetimeAsc(
                        "REQUESTED",
                        "Y",
                        listLimit
                )
                .stream()
                .map(reservation ->
                        new ReservationDashboardResponse(
                                reservation.getReservationId(),
                                reservation.getResource()
                                        .getResourceName(),
                                reservation.getRequester()
                                        .getUserName(),
                                reservation.getStartDatetime(),
                                reservation.getEndDatetime()
                        )
                )
                .toList();
    }

    private List<TaskDashboardResponse> getInProgressTasks(
            Long userId,
            PageRequest listLimit
    ) {
        return taskRepository
                .findByAssigneeUserIdAndTaskStatusAndUseYnOrderByDueDateAsc(
                        userId,
                        "IN_PROGRESS",
                        "Y",
                        listLimit
                )
                .stream()
                .map(task ->
                        new TaskDashboardResponse(
                                task.getTaskId(),
                                task.getTitle(),
                                task.getProgressRate(),
                                task.getAssignee()
                                        .getUserName(),
                                task.getDueDate()
                        )
                )
                .toList();
    }

    private List<ScheduleDashboardResponse> getTodaySchedules(
            Long userId,
            LocalDateTime todayStart,
            LocalDateTime todayEnd
    ) {
        return scheduleRepository
                .findVisibleSchedules(
                        todayStart,
                        todayEnd,
                        userId
                )
                .stream()
                .limit(DASHBOARD_LIST_SIZE)
                .map(schedule ->
                        new ScheduleDashboardResponse(
                                schedule.getScheduleId(),
                                schedule.getTitle(),
                                schedule.getStartDatetime(),
                                schedule.getEndDatetime()
                        )
                )
                .toList();
    }

    private List<MessageDashboardResponse> getReceivedMessages(
            Long userId,
            PageRequest listLimit
    ) {
        return messageRepository
                .findByReceiverUserIdAndReceiverDeleteYnOrderByCreatedAtDesc(
                        userId,
                        "N",
                        listLimit
                )
                .stream()
                .map(message ->
                        new MessageDashboardResponse(
                                message.getMessageId(),
                                message.getTitle(),
                                message.getSender()
                                        .getUserName(),
                                message.getCreatedAt(),
                                "Y".equals(message.getReadYn())
                        )
                )
                .toList();
    }

    private List<ChangeHistoryDashboardResponse> getRecentChanges(
            PageRequest listLimit
    ) {
        return jdbcTemplate.query(
                """
                        select
                            l.log_id,
                            l.action_type,
                            l.table_name,
                            concat(l.table_name, '#', l.record_id) as description,
                            u.user_name,
                            l.created_at
                        from audit_logs l
                        join users u on u.user_id = l.actor_id
                        order by l.created_at desc
                        limit ?
                        """,
                (rs, rowNum) ->
                        new ChangeHistoryDashboardResponse(
                                rs.getLong("log_id"),
                                rs.getString("action_type"),
                                rs.getString("table_name"),
                                rs.getString("description"),
                                rs.getString("user_name"),
                                toLocalDateTime(
                                        rs.getTimestamp("created_at")
                                )
                        ),
                listLimit.getPageSize()
        );
    }

    private LocalDateTime toLocalDateTime(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    private CustomUserDetails getCurrentUser(
            Authentication authentication
    ) {
        if(authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)){
            throw new IllegalArgumentException(
                    "로그인이 필요합니다"
            );
        }
        return userDetails;
    }

    private boolean hasAdminRole(
            Authentication authentication
    ){
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                );
    }
}
