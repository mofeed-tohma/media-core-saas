package com.saas.media_core.repository;

import com.saas.media_core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {
    
    
    Optional<User> findByEmail(String email);
}