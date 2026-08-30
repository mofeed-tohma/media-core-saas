package com.saas.media_core.controller;

import com.saas.media_core.entity.User;
import com.saas.media_core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Digital Wallet", description = "نظام إدارة رصيد المستخدمين")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final UserRepository userRepository;

    @PostMapping("/recharge")
    @Operation(summary = "شحن رصيد المستخدم", description = "يضيف نقاطاً إلى محفظة المستخدم الحالي")
    public ResponseEntity<String> rechargeWallet(Authentication authentication, @RequestParam Integer amount) {
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("مبلغ الشحن يجب أن يكون أكبر من الصفر!");
        }

        
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود!"));

        
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);

        return ResponseEntity.ok("تم شحن المحفظة بنجاح! الرصيد الحالي: " + user.getBalance());
    }

    @GetMapping("/balance")
    @Operation(summary = "جلب رصيد المستخدم", description = "يعيد الرصيد الحالي لمحفظة المستخدم")
    public ResponseEntity<Integer> getBalance(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود!"));
        
        return ResponseEntity.ok(user.getBalance());
    }
}