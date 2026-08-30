package com.saas.media_core.service;

import com.saas.media_core.entity.User;
import com.saas.media_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;

    @Transactional
    public int getOrInitBalance(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("المستخدم غير موجود!"));

        
        if (user.getBalance() == null) {
            user.setBalance(100);
            userRepository.save(user);
        }
        return user.getBalance();
    }

    @Transactional
    public void deductBalance(String userEmail, int cost) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("المستخدم غير موجود!"));

        int currentBalance = user.getBalance() != null ? user.getBalance() : 100;

        if (currentBalance < cost) {
            throw new IllegalStateException("رصيد المحفظة غير كافٍ لإتمام العملية! يرجى شحن رصيدك.");
        }

        user.setBalance(currentBalance - cost);
        userRepository.save(user);
    }
}