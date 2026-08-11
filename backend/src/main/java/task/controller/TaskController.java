package task.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import security.CustomUserDetails;
import task.dto.*;
import task.service.TaskService;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {


    private final TaskService taskService;



    // 업무 목록 조회
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            Pageable pageable
    ){

        return ResponseEntity.ok(
                taskService.getTasks(pageable)
        );

    }



    // 업무 상세 조회
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long taskId
    ){

        return ResponseEntity.ok(
                taskService.getTask(taskId)
        );

    }





    // 생성
    @PostMapping
    public ResponseEntity<Long> createTask(
            Authentication authentication,
            @Valid @RequestBody TaskCreateRequest request
    ){

        return ResponseEntity.ok(
                taskService.createTask(currentUserId(authentication), request)
        );

    }





    // 수정
    @PutMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(
            @PathVariable Long taskId,
            Authentication authentication,
            @RequestBody TaskUpdateRequest request
    ){

        taskService.updateTask(
                taskId,
                currentUserId(authentication),
                request
        );


        return ResponseEntity.ok().build();

    }





    // 삭제
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            Authentication authentication
    ){

        taskService.deleteTask(taskId, currentUserId(authentication));


        return ResponseEntity.ok().build();

    }





    // 상태 변경
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long taskId,
            Authentication authentication,
            @RequestBody TaskStatusRequest request
    ){

        taskService.changeStatus(
                taskId,
                currentUserId(authentication),
                request
        );


        return ResponseEntity.ok().build();

    }





    // 진행률 변경
    @PatchMapping("/{taskId}/progress")
    public ResponseEntity<Void> changeProgress(
            @PathVariable Long taskId,
            Authentication authentication,
            @RequestBody TaskProgressRequest request
    ){

        taskService.changeProgress(
                taskId,
                currentUserId(authentication),
                request
        );


        return ResponseEntity.ok().build();

    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        return principal.getUserId();
    }

}
