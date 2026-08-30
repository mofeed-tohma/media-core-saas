package com.saas.media_core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Media Compression SaaS API",
                description = "واجهة برمجة التطبيقات لمنصة معالجة وضغط الملفات الاحترافية",
                version = "1.0.0",
                contact = @Contact(
                        name = "Support Team",
                        email = "support@media-saas.com"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth") 
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "يرجى إدخال رمز الـ JWT (Bearer Token) هنا للوصول إلى المسارات المحمية",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}