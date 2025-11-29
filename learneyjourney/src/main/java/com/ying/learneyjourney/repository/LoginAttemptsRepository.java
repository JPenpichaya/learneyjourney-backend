package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.LoginAttempts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginAttemptsRepository extends JpaRepository<LoginAttempts, UUID> {
}
