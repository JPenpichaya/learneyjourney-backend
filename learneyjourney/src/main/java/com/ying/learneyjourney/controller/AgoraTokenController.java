package com.ying.learneyjourney.controller;
import com.ying.learneyjourney.dto.request.AgoraTokenRequest;
import com.ying.learneyjourney.dto.response.AgoraTokenResponse;
import com.ying.learneyjourney.service.agora.AgoraTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agora")
public class AgoraTokenController {

    private final AgoraTokenService agoraTokenService;

    public AgoraTokenController(AgoraTokenService agoraTokenService) {
        this.agoraTokenService = agoraTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<AgoraTokenResponse> generateToken(@Valid @RequestBody AgoraTokenRequest request) {
        AgoraTokenResponse response = agoraTokenService.generateRtcToken(
                request.getChannel(),
                request.getUid()
        );
        return ResponseEntity.ok(response);
    }
}