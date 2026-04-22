package com.ying.learneyjourney.service.agora;
import com.ying.learneyjourney.Util.RtcTokenBuilder2;
import com.ying.learneyjourney.config.AgoraProperties;
import com.ying.learneyjourney.dto.response.AgoraTokenResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgoraTokenService {

    private final AgoraProperties agoraProperties;

    public AgoraTokenService(AgoraProperties agoraProperties) {
        this.agoraProperties = agoraProperties;
    }

    public AgoraTokenResponse generateRtcToken(String rawChannel, long uid) {
        String channel = normalizeChannel(rawChannel);

        if (isBlank(agoraProperties.getAppId())) {
            throw new IllegalStateException("agora.app-id is not configured");
        }

        if (isBlank(agoraProperties.getAppCertificate())) {
            throw new IllegalStateException("agora.app-certificate is not configured");
        }

        if (uid <= 0 || uid > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("uid must be a positive integer");
        }

        int expireSeconds = agoraProperties.getExpireSeconds();
        int currentTs = (int) Instant.now().getEpochSecond();
        int privilegeExpireTs = currentTs + expireSeconds;

        String token = RtcTokenBuilder2.buildTokenWithUid(
                agoraProperties.getAppId(),
                agoraProperties.getAppCertificate(),
                channel,
                (int) uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                privilegeExpireTs
        );

        AgoraTokenResponse res = new AgoraTokenResponse();
        res.setToken(token);
        res.setUid(uid);
        res.setAppId(agoraProperties.getAppId());
        res.setExpiresAt(privilegeExpireTs);

        return res;
    }

    private String normalizeChannel(String channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel is required");
        }

        String normalized = channel.trim().toLowerCase().replaceAll("[^a-z0-9-_]", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("channel is invalid");
        }

        return normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}