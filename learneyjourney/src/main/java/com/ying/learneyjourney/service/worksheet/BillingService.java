package com.ying.learneyjourney.service.worksheet;

import com.ying.learneyjourney.dto.request.CreateCheckoutSessionRequest;
import com.ying.learneyjourney.dto.response.CheckoutSessionResponse;
import com.ying.learneyjourney.dto.response.WalletResponse;
import com.ying.learneyjourney.entity.User;

public interface BillingService {
    WalletResponse getWallet(String userEmail);
    CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request, String userId);
    void handleCheckoutCompleted(String sessionId);
    boolean hasActiveSubscription(User user);
    void consumeExport(User user);
}
