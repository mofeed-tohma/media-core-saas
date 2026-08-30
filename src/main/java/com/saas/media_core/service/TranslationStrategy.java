package com.saas.media_core.service;


public interface TranslationStrategy {
    String translate(String text, String targetLanguage);
    String getEngineName();
}