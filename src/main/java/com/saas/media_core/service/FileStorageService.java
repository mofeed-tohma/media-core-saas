package com.saas.media_core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    
    private static final String UPLOAD_DIR = "uploads/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", e.getMessage());
            
            throw new IllegalStateException("لم يتمكن النظام من إنشاء مجلد تخزين الملفات.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = Paths.get(UPLOAD_DIR + fileName);
            
            Files.copy(file.getInputStream(), targetLocation);
            
            return targetLocation.toString();

        } catch (IOException ex) {
            log.error("Error storing file {}: {}", file.getOriginalFilename(), ex.getMessage());
            throw new IllegalStateException("حدث خطأ أثناء حفظ الملف: " + file.getOriginalFilename(), ex);
        }
    }

    public Resource loadFileAsResource(String storedFilePath) {
        try {
            Path filePath = Paths.get(storedFilePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                log.warn("File not found at path: {}", storedFilePath);
                throw new IllegalArgumentException("عفواً، الملف غير موجود على السيرفر.");
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL for file path {}: {}", storedFilePath, ex.getMessage());
            throw new IllegalArgumentException("حدث خطأ أثناء محاولة قراءة الملف، مسار غير صالح.", ex);
        }
    }
}