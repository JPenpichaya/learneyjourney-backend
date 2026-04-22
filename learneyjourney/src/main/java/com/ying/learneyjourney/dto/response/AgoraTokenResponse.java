package com.ying.learneyjourney.dto.response;

import lombok.Data;

@Data
public class AgoraTokenResponse {
    private String token;
    private String appId;
    private long uid;
    private long expiresAt;
}
