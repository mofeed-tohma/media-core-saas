package com.saas.media_core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig {

    @Bean
    @SuppressWarnings("java:S5122")
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        
        config.setAllowCredentials(true);
        
        
        config.addAllowedOriginPattern("*");
        
        
        config.addAllowedHeader("*");
        
        
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}