package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.request.CreateCheckoutSessionRequest;
import com.ying.learneyjourney.dto.response.CheckoutSessionResponse;
import com.ying.learneyjourney.dto.response.WalletResponse;

public interface BillingService {
    WalletResponse getWallet(String userEmail);
    CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request, String userId);
    void handleCheckoutCompleted(String sessionId);
    boolean hasActiveSubscription(String userEmail);
    void consumeExport(String userEmail);
}
