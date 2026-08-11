package schedule.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import schedule.dto.ScheduleCreateRequest;
import schedule.dto.ScheduleDetailResponse;
import schedule.dto.ScheduleResponse;
import schedule.dto.ScheduleUpdateRequest;
import schedule.service.ScheduleService;
import security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 월간 조회
    @GetMapping("/monthly")
    public ResponseEntity<List<ScheduleResponse>> monthly(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Authentication authentication
            ){

        return  ResponseEntity.ok(
                scheduleService.getMonthlySchedule(
                        start,
                        end,
                        currentUserId(authentication)
                )
        );
    }

    // 일간 조회
    @GetMapping("/daily")
    public ResponseEntity<List<ScheduleResponse>> daily(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                scheduleService.getDailySchedule(
                        start,
                        end,
                        currentUserId(authentication)
                )
        );
    }

    // 생성
    @PostMapping
    public ResponseEntity<Long> create(
            Authentication authentication,
            @Valid
            @RequestBody ScheduleCreateRequest request
    ){

        return ResponseEntity.ok(
                scheduleService.createSchedule(
                        currentUserId(authentication),
                        request
                )
        );

    }

    // 상세 조회
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> detail(
            @PathVariable Long scheduleId,
            Authentication authentication
    ){

        return ResponseEntity.ok(
                scheduleService.getDetail(
                        scheduleId,
                        currentUserId(authentication)
                )
        );

    }





    // 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> update(
            @PathVariable Long scheduleId,
            Authentication authentication,
            @Valid
            @RequestBody ScheduleUpdateRequest request
    ){

        scheduleService.update(
                scheduleId,
                currentUserId(authentication),
                request
        );


        return ResponseEntity.ok().build();

    }





    // 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long scheduleId,
            Authentication authentication
    ){

        scheduleService.delete(
                scheduleId,
                currentUserId(authentication)
        );


        return ResponseEntity.ok().build();

    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        return principal.getUserId();
    }
}
