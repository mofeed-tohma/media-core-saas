package com.saas.media_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiNeuralTranslationProcessorService {

    
    private final TranslationStrategy translationStrategy;

    public byte[] process(String inputData) {
        return process(inputData, "ar");
    }

    public byte[] process(String inputData, String targetLang) {
        log.info("بدء المعالجة باستخدام محرك: {}", translationStrategy.getEngineName());

        try {
            
            String translatedContent = translationStrategy.translate(inputData, targetLang);
            
            
            String finalPayload = buildStandardOutput(inputData.length(), targetLang, translatedContent);
            return finalPayload.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("فشل في معالجة الترجمة: {}", e.getMessage(), e);
            throw new IllegalStateException("حدث خطأ داخلي أثناء معالجة النص في المحرك اللغوي.");
        }
    }

    private String buildStandardOutput(int length, String targetLang, String content) {
        
        return String.format("""
                === [AI Neural Translation Output] ===
                Target Language: %s
                Analyzed Length: %d chars
                Engine: %s
                ----------------------------------------
                %s""",
                targetLang.toUpperCase(),
                length,
                translationStrategy.getEngineName(),
                content
        );
    }
}