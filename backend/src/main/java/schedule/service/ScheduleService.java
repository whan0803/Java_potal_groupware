package schedule.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import schedule.dto.ScheduleCreateRequest;
import schedule.dto.ScheduleDetailResponse;
import schedule.dto.ScheduleResponse;
import schedule.dto.ScheduleUpdateRequest;
import schedule.entity.Schedule;
import schedule.repository.ScheduleRepository;
import user.entity.User;
import user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 월간 조회
    public List<ScheduleResponse> getMonthlySchedule(
            LocalDateTime start,
            LocalDateTime end,
            Long userId
    ){
        validatePeriod(
                start,
                end
        );

        return scheduleRepository.findVisibleSchedules(
                        start,
                        end,
                        userId
                ).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    // 일간 조회
    public List<ScheduleResponse> getDailySchedule(
            LocalDateTime start,
            LocalDateTime end,
            Long userId
    )
    {
        validatePeriod(
                start,
                end
        );

        return scheduleRepository.findVisibleSchedules(
                        start,
                        end,
                        userId
                ).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    // 생성
    @Transactional
    public Long createSchedule(
            Long userId,
            ScheduleCreateRequest request
    ){

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다"
                        ));

        validatePeriod(
                request.startDatetime(),
                request.endDatetime()
        );


        Schedule schedule =
                Schedule.create(
                        user,
                        request.title(),
                        request.content(),
                        request.location(),
                        request.scheduleType(),
                        request.startDatetime(),
                        request.endDatetime(),
                        request.allDayYn()
                );


        return scheduleRepository.save(schedule)
                .getScheduleId();

    }

    // 상세 조회
    public ScheduleDetailResponse getDetail(
            Long scheduleId,
            Long userId
    ){
        Schedule schedule = findActiveSchedule(scheduleId);

        validateReadable(
                schedule,
                userId
        );

        return ScheduleDetailResponse.from(schedule);
    }

    // 수정
    @Transactional
    public void update(
            Long scheduleId,
            Long userId,
            ScheduleUpdateRequest request
    ){
        Schedule schedule = findActiveSchedule(scheduleId);

        validateWriter(
                schedule,
                userId
        );

        validatePeriod(
                request.startDatetime(),
                request.endDatetime()
        );

        schedule.update(
                request.title(),
                request.content(),
                request.location(),
                request.startDatetime(),
                request.endDatetime(),
                userId
        );
    }

    // 삭제
    @Transactional
    public void delete(
            Long scheduleId,
            Long userId
    ){

        Schedule schedule =
                findActiveSchedule(scheduleId);


        validateWriter(
                schedule,
                userId
        );


        schedule.delete(userId);

    }

    private Schedule findSchedule(
            Long scheduleId
    ){
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "일정을 찾을 수 없습니다"
                ));

    }

    private Schedule findActiveSchedule(
            Long scheduleId
    ) {
        Schedule schedule = findSchedule(scheduleId);

        if (!"Y".equals(schedule.getUseYn())) {
            throw new IllegalStateException(
                    "삭제되었거나 사용 중지된 일정입니다"
            );
        }

        return schedule;
    }

    private void validateWriter(
            Schedule schedule,
            Long userId
    ){
        if(!schedule.getUser()
                .getUserId()
                .equals(userId)) {
            throw new IllegalArgumentException(
                    "작성자만 수정 가능합니다"
            );
        }
    }

    private void validateReadable(
            Schedule schedule,
            Long userId
    ) {
        boolean publicSchedule =
                "PUBLIC".equals(schedule.getScheduleType());

        boolean owner =
                schedule.getUser()
                        .getUserId()
                        .equals(userId);

        if (!publicSchedule && !owner) {
            throw new IllegalArgumentException(
                    "일정을 조회할 권한이 없습니다"
            );
        }
    }

    private void validatePeriod(
            LocalDateTime startDatetime,
            LocalDateTime endDatetime
    ) {
        if (endDatetime.isBefore(startDatetime)) {
            throw new IllegalArgumentException(
                    "종료 시간은 시작 시간보다 빠를 수 없습니다"
            );
        }
    }
}
