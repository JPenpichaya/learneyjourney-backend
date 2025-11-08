package com.ying.learneyjourney.controller;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @Value("${LIVEKIT_URL}")
    private String livekitUrl;

    @Value("${LIVEKIT_API_KEY}")
    private String apiKey;

    @Value("${LIVEKIT_API_SECRET}")
    private String apiSecret;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> createToken(@RequestBody TokenRequest req) {
        if (req == null || isBlank(req.room) || isBlank(req.identity)) {
            return ResponseEntity.badRequest().build();
        }

        // Build a LiveKit access token
        AccessToken at = new AccessToken(apiKey, apiSecret);
        at.setIdentity(req.identity);           // who joins
        if (req.name != null) at.setName(req.name);
        if (req.metadata != null) at.setMetadata(req.metadata);

        // Grants: join specific room; can publish & subscribe by default
        at.addGrants(new RoomJoin(true), new RoomName(req.room));

        String jwt = at.toJwt(); // signed token

        return ResponseEntity.ok(new TokenResponse(livekitUrl, jwt));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static class TokenRequest {
        public String room;
        public String identity;
        public String name;       // optional
        public String metadata;   // optional (e.g., avatar, role JSON)
    }

    public static class TokenResponse {
        public String url;
        public String token;
        public TokenResponse(String url, String token) { this.url = url; this.token = token; }
    }
}
