package com.ying.learneyjourney.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class FirebaseUserService {
    private final UserRepository userRepository;

    public FirebaseUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verify Firebase ID token, then find/create a User in the DB.
     */
    @Transactional
    public User authenticateAndSyncUser(String idToken) throws Exception {
        // 1. Verify token with Firebase
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

        UUID uid = UUID.fromString(decoded.getUid());
        String email = decoded.getEmail();
        String name = (String) decoded.getClaims().getOrDefault("name", null);
        String picture = (String) decoded.getClaims().getOrDefault("picture", null);

        // 2. Find user by UID
        return userRepository.findById(uid)
                .map(user -> updateExistingUser(user, email, name, picture))
                .orElseGet(() -> createNewUser(uid, email, name, picture));
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

    private User createNewUser(UUID uid, String email, String name, String picture) {
        User user = new User();
        user.setId(uid);
        user.setEmail(email);
        user.setDisplayName(name);
        user.setPhotoUrl(picture);
        return userRepository.save(user);
    }
}
