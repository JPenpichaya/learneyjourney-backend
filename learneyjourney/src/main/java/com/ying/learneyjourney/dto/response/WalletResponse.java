package com.ying.learneyjourney.dto.response;

public record WalletResponse(
        String planType,
        boolean hasActiveSubscription,
        int credits,
        int freeExportsUsed,
        int freeExportsLimit,
        int dailyGenerationsUsed,
        int dailyGenerationLimit
) {
}