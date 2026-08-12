package task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import task.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByUseYn(
            String useYn,
            Pageable pageable
    );

    long countByAssigneeUserIdAndTaskStatusAndUseYn(
            Long assigneeId,
            String taskStatus,
            String useYn
    );

    List<Task> findByAssigneeUserIdAndTaskStatusAndUseYnOrderByDueDateAsc(
            Long assigneeId,
            String taskStatus,
            String useYn,
            Pageable pageable
    );
}
