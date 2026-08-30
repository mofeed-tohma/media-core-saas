package com.saas.media_core.controller;

import com.saas.media_core.entity.User;
import com.saas.media_core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "إدارة بيانات المستخدم الشخصية")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // تعريف النص المتكرر كمتغير ثابت لحل تحذير (Magic String)
    private static final String USER_NOT_FOUND_MSG = "المستخدم غير موجود!";

    @GetMapping("/me")
    @Operation(summary = "جلب بيانات المستخدم الحالي")
    public ResponseEntity<Object> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("غير مصرح");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));

        UserProfileResponse response = new UserProfileResponse(
                user.getFullName(), 
                user.getPhone(),    
                user.getEmail(),
                user.getBalance()
        );
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    @Operation(summary = "تحديث بيانات الملف الشخصي")
    public ResponseEntity<Object> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        String currentEmail = authentication.getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        return ResponseEntity.ok("تم تحديث البيانات بنجاح");
    }

    @PutMapping("/change-password")
    @Operation(summary = "تغيير كلمة المرور")
    public ResponseEntity<Object> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty() ||
            request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("يرجى إدخال كلمة المرور الحالية والجديدة!");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("كلمة المرور الحالية غير صحيحة!");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("كلمة المرور الجديدة غير مطابقة للتأكيد!");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("تم تغيير كلمة المرور بنجاح");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserProfileResponse {
        private String fullName;
        private String phone;
        private String email;
        private Integer balance;
    }

    @Data
    public static class UpdateProfileRequest {
        private String fullName;
        private String phone;
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }
}