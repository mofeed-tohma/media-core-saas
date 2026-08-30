package com.saas.media_core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class GarbageCollectorService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Scheduled(fixedRate = 60000)
    public void cleanOldFiles() {
        log.info("Starting automated cleanup process for old files...");

        File directory = new File(uploadDir);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("Upload directory does not exist, skipping cleanup.");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("Directory is empty, no files to delete.");
            return;
        }

        int deletedCount = 0;
        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

        for (File file : files) {
            try {
                Path filePath = Paths.get(file.getAbsolutePath());
                BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                Instant fileCreationTime = attr.creationTime().toInstant();

                if (fileCreationTime.isBefore(twentyFourHoursAgo)) {
                    
                    boolean deleted = Files.deleteIfExists(filePath);
                    if (deleted) {
                        log.info("Deleted old file: {}", file.getName());
                        deletedCount++;
                    }
                }
            } catch (IOException | SecurityException e) {
                
                log.error("Error occurred while trying to delete file: {}", file.getName(), e);
            }
        }

        log.info("Cleanup completed. Deleted {} files.", deletedCount);
    }
}