package com.saas.media_core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class CompressionEngineService {

    
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
}