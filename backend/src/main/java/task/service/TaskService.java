package task.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task.dto.*;
import task.entity.Task;
import task.repository.TaskRepository;
import user.entity.User;
import user.repository.UserRepository;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {


    private final TaskRepository taskRepository;
    private final UserRepository userRepository;



    // 업무 목록 조회
    public Page<TaskResponse> getTasks(Pageable pageable){

        return taskRepository.findByUseYn("Y", pageable)
                .map(TaskResponse::from);

    }



    // 업무 상세 조회
    public TaskResponse getTask(Long taskId){

        Task task = findTask(taskId);

        return TaskResponse.from(task);

    }



    // 생성
    @Transactional
    public Long createTask(Long currentUserId, TaskCreateRequest request){


        User requester =
                userRepository.findById(currentUserId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "요청자를 찾을 수 없습니다."
                                )
                        );


        User assignee =
                userRepository.findById(request.assigneeId())
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "담당자를 찾을 수 없습니다."
                                )
                        );



        Task task = Task.create(
                requester,
                assignee,
                request.title(),
                request.content(),
                normalizeTaskStatus(request.taskStatus()),
                normalizePriority(request.priority()),
                request.startDate(),
                request.dueDate()
        );


        taskRepository.save(task);


        return task.getTaskId();

    }




    // 수정
    @Transactional
    public void updateTask(
            Long taskId,
            Long currentUserId,
            TaskUpdateRequest request
    ){

        Task task = findTask(taskId);
        validateOwner(task, currentUserId);



        User assignee =
                userRepository.findById(request.assigneeId())
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "담당자를 찾을 수 없습니다."
                                )
                        );



        task.update(
                assignee,
                request.title(),
                request.content(),
                normalizeTaskStatus(request.taskStatus()),
                normalizePriority(request.priority()),
                request.startDate(),
                request.dueDate()
        );

    }




    // 삭제
    @Transactional
    public void deleteTask(Long taskId, Long currentUserId){


        Task task = findTask(taskId);
        validateOwner(task, currentUserId);


        task.delete();

    }





    // 상태 변경
    @Transactional
    public void changeStatus(
            Long taskId,
            Long currentUserId,
            TaskStatusRequest request
    ){

        Task task = findTask(taskId);
        validateOwner(task, currentUserId);


        task.changeStatus(
                normalizeTaskStatus(request.taskStatus())
        );

    }




    // 진행률 변경
    @Transactional
    public void changeProgress(
            Long taskId,
            Long currentUserId,
            TaskProgressRequest request
    ){

        Task task = findTask(taskId);
        validateOwner(task, currentUserId);


        task.changeProgress(
                request.progressRate()
        );

    }





    private Task findTask(Long taskId){

        return taskRepository.findById(taskId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "업무 정보를 찾을 수 없습니다."
                        )
                );

    }

    private void validateOwner(Task task, Long currentUserId) {
        if (!task.getRequester().getUserId().equals(currentUserId)) {
            throw new IllegalStateException(
                    "업무를 등록한 사용자만 수정할 수 있습니다."
            );
        }
    }

    private String normalizeTaskStatus(String taskStatus) {
        if (taskStatus == null || taskStatus.isBlank()) {
            return "READY";
        }

        return switch (taskStatus.trim().toUpperCase()) {
            case "TODO" -> "READY";
            case "DONE" -> "COMPLETED";
            default -> taskStatus.trim().toUpperCase();
        };
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "NORMAL";
        }

        return priority.trim().toUpperCase();
    }

}
