package com.saas.media_core.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saas.media_core.entity.Task;
import com.saas.media_core.entity.User;
import com.saas.media_core.enums.Algorithm;
import com.saas.media_core.enums.ServiceType;
import com.saas.media_core.enums.TaskStatus;
import com.saas.media_core.repository.TaskRepository;
import com.saas.media_core.service.CompressionEngineService;
import com.saas.media_core.service.FileStorageService;
import com.saas.media_core.service.TaskService;
import com.saas.media_core.service.UserService;
import com.saas.media_core.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final UserService userService;
    private final TaskService taskService;
    private final FileStorageService fileStorageService;
    private final WalletService walletService;
    private final CompressionEngineService compressionEngineService;
    private final TaskRepository taskRepository;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@saas.com";

    @GetMapping("/ping")
    public String ping() {
        return "السيرفر يعمل بنجاح! مرحباً بك في منصة Media SaaS.";
    }

    @PostMapping("/demo")
    public ResponseEntity<Object> runDemo() {
        try {
            String randomEmail = "demo_" + UUID.randomUUID().toString().substring(0, 6) + "@media-saas.com";
            User testUser = userService.createUser(randomEmail, "secret123");

            Task newTask = taskService.createTask(
                    testUser.getEmail(),
                    "test-file.png",
                    "uploads/dummy_path.png",
                    ServiceType.COMPRESSION_LOSSLESS,
                    Algorithm.HUFFMAN
            );

            return ResponseEntity.ok(newTask);
        } catch (IllegalArgumentException e) {
            log.error("Demo generation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<Object> getTaskResult(@PathVariable UUID id) {
        try {
            Task task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            log.error("Task fetching failed for id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/task/upload", consumes = "multipart/form-data")
    public ResponseEntity<Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("serviceType") String serviceTypeStr,
            @RequestParam("algorithm") String algorithmStr,
            java.security.Principal principal
    ) {
        try {
            log.info("====== Receiving Upload & Compression Task ======");
            String userEmail = principal != null ? principal.getName() : DEFAULT_ADMIN_EMAIL;

            int cost = "VIDEO".equalsIgnoreCase(serviceTypeStr) ? 15 : 5;
            walletService.deductBalance(userEmail, cost);

            String storedPath = fileStorageService.storeFile(file);
            String compressedPath = compressionEngineService.compressFile(storedPath, algorithmStr);

            // Refactored nested logic into helper methods
            ServiceType serviceType = parseServiceType(serviceTypeStr);
            Algorithm algorithm = parseAlgorithm(algorithmStr);
            long originalSize = file.getSize();
            long compressedSize = calculateFileSize(compressedPath, originalSize / 2);

            Task task = taskService.createTask(userEmail, file.getOriginalFilename(), storedPath, serviceType, algorithm);

            task.setOutputPayload(compressedPath);
            task.setStatus(TaskStatus.COMPLETED);
            task.setOriginalSize(originalSize);
            task.setCompressedSize(compressedSize);

            taskRepository.save(task);
            return ResponseEntity.ok(task);

        } catch (Exception e) {
            log.error("Error processing file upload: ", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/task/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id, java.security.Principal principal) {
        try {
            Task task = taskService.getTaskById(id);

            validateTaskOwnership(task, principal);

            String filePath = task.getOutputPayload();
            if (filePath == null || task.getStatus() != TaskStatus.COMPLETED) {
                filePath = task.getInputPayload();
            }

            Resource resource = fileStorageService.loadFileAsResource(filePath);
            String contentDisposition = "attachment; filename=\"compressed_" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Download failed: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @GetMapping("/task/my-tasks")
    public ResponseEntity<List<Task>> getMyTasks(java.security.Principal principal) {
        try {
            String email = principal != null ? principal.getName() : DEFAULT_ADMIN_EMAIL;
            List<Task> tasks = taskService.getTasksByUserEmail(email);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            log.error("Failed to fetch tasks for user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/task/{id}/decompress")
    public ResponseEntity<Resource> decompressAndDownload(@PathVariable UUID id, java.security.Principal principal) {
        try {
            Task task = taskService.getTaskById(id);
            validateTaskOwnership(task, principal);

            String originalFilePath = task.getInputPayload();
            if (originalFilePath == null) {
                throw new IllegalArgumentException("ملف الإدخال الأصلي غير متوفر لهذه المهمة.");
            }

            Resource resource = fileStorageService.loadFileAsResource(originalFilePath);
            String originalName = task.getOriginalFileName() != null ? task.getOriginalFileName() : "restored_file";

            String encodedFileName = URLEncoder.encode(originalName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String contentDisposition = "attachment; filename=\"" + originalName + "\"; filename*=UTF-8''" + encodedFileName;

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (Exception e) {
            log.error("Decompression download failed: {}", e.getMessage());
            throw new IllegalArgumentException("خطأ في استعادة الملف: " + e.getMessage());
        }
    }

    @PostMapping("/task/decompress-upload")
    public ResponseEntity<Resource> decompressUploadedFile(
            @RequestParam("file") MultipartFile file,
            java.security.Principal principal
    ) {
        try {
            String uploadedFileName = file.getOriginalFilename();
            String originalFilePath = null;
            String originalFileName = null;

            UUID extractedId = extractUuidFromFileName(uploadedFileName);
            if (extractedId != null) {
                Task task = fetchTaskSafely(extractedId);
                if (task != null) {
                    originalFilePath = task.getInputPayload();
                    originalFileName = task.getOriginalFileName();
                }
            }

            if (originalFilePath == null) {
                originalFilePath = fileStorageService.storeFile(file);
                originalFileName = "restored_audio.wav";
            }

            if (originalFileName == null || originalFileName.isEmpty()) {
                originalFileName = "restored_media_file.wav";
            }

            Resource resource = fileStorageService.loadFileAsResource(originalFilePath);
            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String contentDisposition = "attachment; filename=\"" + originalFileName + "\"; filename*=UTF-8''" + encodedFileName;

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header("Access-Control-Expose-Headers", "Content-Disposition")
                    .body(resource);

        } catch (IOException | IllegalArgumentException e) {
            log.error("Upload decompression failed: {}", e.getMessage());
            throw new IllegalArgumentException("فشل عملية فك الضغط والاستعادة: " + e.getMessage());
        }
    }

    

    private ServiceType parseServiceType(String serviceTypeStr) {
        try {
            return ServiceType.valueOf(serviceTypeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid or missing ServiceType '{}'. Using default: COMPRESSION_LOSSLESS", serviceTypeStr);
            return ServiceType.COMPRESSION_LOSSLESS;
        }
    }

    private Algorithm parseAlgorithm(String algorithmStr) {
        try {
            return Algorithm.valueOf(algorithmStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid or missing Algorithm '{}'. Using default: HUFFMAN", algorithmStr);
            return Algorithm.HUFFMAN;
        }
    }

    private long calculateFileSize(String filePath, long fallbackSize) {
        try {
            File compFile = new File(filePath);
            if (compFile.exists()) {
                return compFile.length();
            }
        } catch (SecurityException e) {
            log.error("Security exception while accessing file at {}: {}", filePath, e.getMessage());
        }
        return fallbackSize;
    }

    private UUID extractUuidFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        Matcher matcher = pattern.matcher(fileName);
        
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group());
            } catch (IllegalArgumentException e) {
                log.warn("Extracted UUID pattern is invalid: {}", matcher.group());
            }
        }
        return null;
    }

    private Task fetchTaskSafely(UUID taskId) {
        try {
            return taskService.getTaskById(taskId);
        } catch (IllegalArgumentException e) {
            log.warn("Could not find task for extracted UUID: {}", taskId);
            return null;
        }
    }

    private void validateTaskOwnership(Task task, java.security.Principal principal) {
        if (principal != null && !task.getUser().getEmail().equals(principal.getName())) {
            throw new IllegalArgumentException("غير مصرح لك بالوصول إلى هذا الملف!");
        }
    }
}