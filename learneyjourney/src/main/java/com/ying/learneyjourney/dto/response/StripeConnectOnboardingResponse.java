package com.ying.learneyjourney.dto.response;

public record StripeConnectOnboardingResponse(
        String accountId,
        String onboardingUrl
) {}