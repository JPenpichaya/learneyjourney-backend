package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.service.FirebaseUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FirebaseUserService firebaseUserService;

    public AuthController(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }

        String idToken = authHeader.substring(7);

        try {
            // 👉 Verify token + create/find user
            User user = firebaseUserService.authenticateAndSyncUser(idToken);

            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setDisplayName(user.getDisplayName());
            dto.setPhotoUrl(user.getPhotoUrl());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace(); // will show up in Cloud Run logs
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Firebase token: " + e.getMessage());
        }
    }
}
