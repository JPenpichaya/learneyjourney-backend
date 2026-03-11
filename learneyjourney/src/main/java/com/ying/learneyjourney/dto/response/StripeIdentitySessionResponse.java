package com.ying.learneyjourney.dto.response;

public record StripeIdentitySessionResponse(
        String verificationSessionId,
        String clientSecret,
        String url,
        String status
) {}
