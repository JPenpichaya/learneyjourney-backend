package com.ying.learneyjourney.dto.response;

public record CheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}