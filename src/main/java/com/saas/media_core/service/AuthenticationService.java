package com.saas.media_core.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.media_core.dto.AuthRequest;
import com.saas.media_core.dto.AuthResponse;
import com.saas.media_core.entity.User;
import com.saas.media_core.repository.UserRepository;
import com.saas.media_core.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    
    private static final String LOGIN_ERROR_MSG = "البريد الإلكتروني أو كلمة المرور غير صحيحة";
    private static final String USER_NOT_FOUND_MSG = "المستخدم غير موجود";

    public AuthResponse register(AuthRequest request) {
        var user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        
        repository.save(user);
        
        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("فشل محاولة تسجيل الدخول للحساب {}: {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException(LOGIN_ERROR_MSG);
        }
        
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG));
                
        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}