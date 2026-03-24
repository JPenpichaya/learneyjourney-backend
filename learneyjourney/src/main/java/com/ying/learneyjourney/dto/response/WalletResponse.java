package com.ying.learneyjourney.dto.response;

public record WalletResponse(
        String planType,
        boolean hasActiveSubscription,
        int credits
) {
}