package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DevTestController {
    private final BillingService billingService;

    @PostMapping("/api/v1/dev/complete-checkout/{sessionId}")
    public void completeCheckout(@PathVariable String sessionId) {
        billingService.handleCheckoutCompleted(sessionId);
    }
}
