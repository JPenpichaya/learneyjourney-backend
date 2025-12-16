package com.ying.learneyjourney.Util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthUtil {
    public static FirebaseToken verify(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            return FirebaseAuth.getInstance().verifyIdToken(token);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid Firebase token", e);
        }
    }

    public String getUserIdFromToken(String bearerToken) throws Exception {
        String token = bearerToken.replace("Bearer ", "");

        FirebaseToken decodedToken =
                FirebaseAuth.getInstance().verifyIdToken(token);

        return decodedToken.getUid(); // 👈 userId
    }
}
