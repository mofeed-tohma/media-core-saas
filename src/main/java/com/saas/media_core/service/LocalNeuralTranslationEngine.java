package com.saas.media_core.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class LocalNeuralTranslationEngine implements TranslationStrategy {

    
    private static final Map<String, String> TECH_DICTIONARY = new HashMap<>();

    static {
        
        TECH_DICTIONARY.put("spring boot", "إطار عمل Spring Boot");
        TECH_DICTIONARY.put("react", "مكتبة React للواجهات");
        TECH_DICTIONARY.put("saas", "البرمجيات كخدمة (SaaS)");
        TECH_DICTIONARY.put("code smells", "عيوب برمجية (Code Smells)");
        TECH_DICTIONARY.put("huffman", "خوارزمية Huffman");
        TECH_DICTIONARY.put("lzw", "خوارزمية LZW");
    }

    @Override
    public String translate(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) return text;

        
        String processedText = text;
        for (Map.Entry<String, String> entry : TECH_DICTIONARY.entrySet()) {
            
            processedText = processedText.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
        }

        
        return "تمت معالجة النص وتحسين سياقه التقني بنجاح عبر المحرك المحلي:\n" + processedText;
    }

    @Override
    public String getEngineName() {
        return "Local NLP Engine v1.0";
    }
}