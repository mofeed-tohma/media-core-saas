package com.saas.media_core.controller;

import com.saas.media_core.dto.AuthRequest;
import com.saas.media_core.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(service.register(request));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping({"/authenticate", "/login"})
    public ResponseEntity<Object> authenticate(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(service.authenticate(request));
        } catch (Exception e) {
            log.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("البريد الإلكتروني أو كلمة المرور غير صحيحة");
        }
    }
}