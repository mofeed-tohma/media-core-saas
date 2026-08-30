package com.saas.media_core.controller;

import com.saas.media_core.service.AiNeuralTranslationProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TranslationController {

    private final AiNeuralTranslationProcessorService translationService;

    @PostMapping("/translate")
    public ResponseEntity<String> translateText(@RequestBody Map<String, String> payload) {
        String inputData = payload.getOrDefault("text", "");
        String targetLang = payload.getOrDefault("targetLang", "ar");
        
        byte[] resultBytes = translationService.process(inputData, targetLang);
        return ResponseEntity.ok(new String(resultBytes, StandardCharsets.UTF_8));
    }
}