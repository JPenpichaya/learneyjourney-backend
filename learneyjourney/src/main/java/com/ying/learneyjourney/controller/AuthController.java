package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.service.FirebaseUserService;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<UserDto> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String idToken = authHeader.substring(7);

        User user = firebaseUserService.authenticateAndSyncUser(idToken);

        // Here you could also create your own JWT if you want.
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setPhotoUrl(user.getPhotoUrl());

        return ResponseEntity.ok(dto);
    }
}
