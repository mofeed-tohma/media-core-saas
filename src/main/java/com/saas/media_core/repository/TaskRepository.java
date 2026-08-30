package com.saas.media_core.repository;

import com.saas.media_core.entity.Task;
import com.saas.media_core.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;



public interface TaskRepository extends JpaRepository<Task, UUID> {

    /** 
     * @param userId 
     * @return 
     */
    List<Task> findByUserId(UUID userId);

    /**
     * @param status حالة المهمة المطلوبة
     * @return قائمة بالمهام التي تطابق الحالة
     */
    List<Task> findByStatus(TaskStatus status);
}