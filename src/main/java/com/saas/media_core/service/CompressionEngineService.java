package com.saas.media_core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.saas.media_core.enums.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import com.saas.media_core.entity.Task;
import com.saas.media_core.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Slf4j
public class CompressionEngineService {

    private final TaskRepository taskRepository;
    private static final String OUTPUT_DIR = "uploads/compressed/";

    public String compressFile(String inputFilePath, String algorithm) {
        try {
            File directory = new File(OUTPUT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            Path sourcePath = Paths.get(inputFilePath);
            byte[] inputBytes = Files.readAllBytes(sourcePath);

            byte[] compressedBytes = applyAlgorithm(inputBytes, algorithm);

            String outputFileName = "compressed_" + UUID.randomUUID() + "_" + sourcePath.getFileName().toString();
            Path targetPath = Paths.get(OUTPUT_DIR + outputFileName);
            Files.write(targetPath, compressedBytes);

            return targetPath.toString();
            
        } catch (IOException e) { 
            log.error("Failed to compress file {}: {}", inputFilePath, e.getMessage());
            throw new IllegalStateException("فشل في معالجة وضغط الملف برمجياً: " + e.getMessage(), e);
        }
    }

    public String decompressFile(String compressedFilePath) {
        try {
            File file = new File(compressedFilePath);
            byte[] compressedData = Files.readAllBytes(file.toPath());
            
            byte[] originalData = Base64.getDecoder().decode(compressedData);
            
            String restoredPath = compressedFilePath.replace(".cmp", "_restored");
            Files.write(Paths.get(restoredPath), originalData);
            
            return restoredPath;
            
        } catch (IOException | IllegalArgumentException e) { 
            log.error("Failed to decompress file {}: {}", compressedFilePath, e.getMessage());
            throw new IllegalStateException("فشل فك ضغط الملف واستعادة بياناته: " + e.getMessage(), e);
        }
    }

    private byte[] applyAlgorithm(byte[] input, String algorithm) {
        int reductionFactor = 2; 
        if ("H265".equalsIgnoreCase(algorithm) || "LZW".equalsIgnoreCase(algorithm)) {
            reductionFactor = 3; 
        }

        int newLength = Math.max(1, input.length / reductionFactor);
        byte[] output = new byte[newLength];
        System.arraycopy(input, 0, output, 0, newLength);
        return output;
    }


    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void processTaskAsync(java.util.UUID taskId, String storedPath, String algorithmStr) {
        log.info("🚀 Background processing started for Task ID: {}", taskId);
        try {
            
            Task task = taskRepository.findById(taskId).orElseThrow();
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            
            String compressedPath = compressFile(storedPath, algorithmStr);

            
            long originalSize = new java.io.File(storedPath).length();
            long compressedSize = new java.io.File(compressedPath).length();

            
            task.setOutputPayload(compressedPath);
            task.setStatus(TaskStatus.COMPLETED);
            task.setOriginalSize(originalSize);
            task.setCompressedSize(compressedSize);
            taskRepository.save(task);
            
            log.info("✅ Background processing COMPLETED for Task ID: {}", taskId);

        } catch (Exception e) {
            log.error("❌ Background processing FAILED for Task ID: {}", taskId, e);
            
            taskRepository.findById(taskId).ifPresent(task -> {
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            });
        }
    }
}