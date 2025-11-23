package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
}
