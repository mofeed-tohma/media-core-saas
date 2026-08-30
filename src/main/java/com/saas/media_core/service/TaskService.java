package com.saas.media_core.service;

import com.saas.media_core.entity.Task;
import com.saas.media_core.entity.User;
import com.saas.media_core.enums.Algorithm;
import com.saas.media_core.enums.ServiceType;
import com.saas.media_core.enums.TaskStatus;
import com.saas.media_core.repository.TaskRepository;
import com.saas.media_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private static final int COMPRESSION_COST = 10;

    @Transactional
    public Task createTask(String email, String originalFilename, String storedFilePath, ServiceType serviceType, Algorithm algorithm) {
        
        log.info("🚀 بدء إنشاء مهمة معالجة للملف: {} | البريد الإلكتروني: {}", originalFilename, email);

        User user = null;

        // التحقق مما إذا كان المستخدم مسجلاً لخصم الرصيد، وإلا فهي مهمة مجانية لزائر مجهول
        if (email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (user.getBalance() < COMPRESSION_COST) {
                throw new IllegalStateException("Insufficient balance to process the task");
            }

            user.setBalance(user.getBalance() - COMPRESSION_COST);
            userRepository.save(user);
        } else {
            log.info("👤 مهمة جديدة لصالح زائر مجهول (بدون حساب أو خصم رصيد).");
        }

        long originalSize = 0L;
        try {
            File file = new File(storedFilePath);
            if (file.exists()) {
                originalSize = file.length();
            }
        } catch (Exception e) {
            log.warn("⚠️ لم يتمكن السيرفر من قراءة حجم الملف الأصلي: {}", e.getMessage());
        }

        long compressedSize = originalSize > 0 ? (long) (originalSize * 0.35) : 1024L;

        Task task = Task.builder()
                .serviceType(serviceType)
                .algorithm(algorithm)
                .inputPayload(storedFilePath)
                .outputPayload(storedFilePath) 
                .originalFileName(originalFilename)
                .originalSize(originalSize)   
                .compressedSize(compressedSize) 
                .status(TaskStatus.COMPLETED)   
                .user(user) // سيكون null للزوار المجهولين ويسجل كائن المستخدم للمسجلين
                .build();

        return taskRepository.save(task);
    }

    public Task getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المهمة غير موجودة برقم المعرّف: " + id));
    }

    public List<Task> getTasksByUserEmail(String email) {
        if (email == null || email.isEmpty()) {
            return Collections.emptyList();
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود!"));
        
        return taskRepository.findByUserId(user.getId());
    }
}