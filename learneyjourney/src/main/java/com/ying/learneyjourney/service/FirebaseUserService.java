package com.ying.learneyjourney.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ying.learneyjourney.constaint.EnumUserRoles;
import com.ying.learneyjourney.dto.LoginAttemptsDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FirebaseUserService {
    private final UserRepository userRepository;
    private final LoginAttemptsService loginAttemptsService;

    public FirebaseUserService(UserRepository userRepository, LoginAttemptsService loginAttemptsService) {
        this.userRepository = userRepository;
        this.loginAttemptsService = loginAttemptsService;
    }

    /**
     * Verify Firebase ID token, then find/create a User in the DB.
     */
    @Transactional
    public User authenticateAndSyncUser(String idToken, HttpServletRequest request) throws Exception {
        // 1. Verify token with Firebase
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

        String uid = decoded.getUid();
        String email = decoded.getEmail();
        String name = (String) decoded.getClaims().getOrDefault("name", null);
        String picture = (String) decoded.getClaims().getOrDefault("picture", null);

        // 2. Find user by UID
        User userLogin = userRepository.findById(uid)
                .map(user -> updateExistingUser(user, email, name, picture))
                .orElseGet(() -> createNewUser(uid, email, name, picture));

        recordLoginAttempts(request, userLogin, true);
        return userLogin;
    }

    public void recordLoginAttempts(HttpServletRequest request, User userLogin, Boolean success) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");

        LoginAttemptsDto loginAttemptsDto = new LoginAttemptsDto();
        loginAttemptsDto.setAttemptTime(LocalDateTime.now());
        loginAttemptsDto.setSuccess(success);
        loginAttemptsDto.setIpAddress(ipAddress);
        loginAttemptsDto.setUserAgent(userAgent);
        loginAttemptsService.recordLoginAttempt(loginAttemptsDto, userLogin);
    }

    private User updateExistingUser(User user, String email, String name, String picture) {
        // Update fields if changed (optional)
        if (email != null && (user.getEmail() == null || !user.getEmail().equals(email))) {
            user.setEmail(email);
        }
        if (name != null && (user.getDisplayName() == null || !user.getDisplayName().equals(name))) {
            user.setDisplayName(name);
        }
        if (picture != null && (user.getPhotoUrl() == null || !user.getPhotoUrl().equals(picture))) {
            user.setPhotoUrl(picture);
        }
        // lastLoginAt updated via @PreUpdate
        return userRepository.save(user);
    }

    private User createNewUser(String uid, String email, String name, String picture) {
        User user = new User();
        user.setId(uid);
        user.setEmail(email);
        user.setDisplayName(name);
        user.setPhotoUrl(picture);
        user.setRole(EnumUserRoles.STUDENT);
        return userRepository.save(user);
    }
}
