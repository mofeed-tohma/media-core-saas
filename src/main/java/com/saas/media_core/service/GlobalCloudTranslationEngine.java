package com.saas.media_core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Primary 
@Slf4j
public class GlobalCloudTranslationEngine implements TranslationStrategy {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String translate(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) return text;

        try {
            
            String url = "https://api.mymemory.translated.net/get";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set(HttpHeaders.USER_AGENT, "MediaCore-SaaS-Engine/1.0");

            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("q", text);
            map.add("langpair", "Autodetect|" + targetLanguage);
            map.add("de", "admin@mediacore.com"); 

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode responseData = rootNode.get("responseData");
                
                if (responseData != null && responseData.has("translatedText")) {
                    String rawTranslation = responseData.get("translatedText").asText();
                    
                    return applyTechnicalCorrections(rawTranslation);
                }
            }
            return text;

        } catch (Exception e) {
            log.error("Cloud Engine Error: {}", e.getMessage());
            return "عذراً، فشل الاتصال بالمحرك السحابي العالمي. يرجى التحقق من اتصال الإنترنت.";
        }
    }

    
    private String applyTechnicalCorrections(String text) {
        return text
                .replace("التمهيد الزنبركي", "Spring Boot")
                .replace("إقلاع الربيع", "Spring Boot")
                .replace("رد فعل", "React")
                .replace("تفاعل", "React")
                .replace("الروائح الصفرية", "خلوه تماماً من العيوب البرمجية (Zero Code Smells)")
                .replace("رائحة خالية من الرموز.", "خلوه تماماً من العيوب البرمجية (Zero Code Smells).")
                .replace("روائح الكود", "عيوب برمجية (Code Smells)");
    }

    @Override
    public String getEngineName() {
        return "Global Cloud AI Engine (Pro)";
    }
}