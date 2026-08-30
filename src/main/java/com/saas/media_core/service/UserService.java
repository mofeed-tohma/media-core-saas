package com.saas.media_core.service;

import com.saas.media_core.entity.User;
import com.saas.media_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; 

    @Transactional
    public User createUser(String email, String password) {
        
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .build();
                
        return userRepository.save(user);
    }

    @Transactional
    public void deductCredits(User user, Integer amount) {
        
        if (user.getBalance() < amount) {
            throw new IllegalStateException("عفواً، رصيدك غير كافٍ لإتمام هذه العملية.");
        }
        
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
    }
}