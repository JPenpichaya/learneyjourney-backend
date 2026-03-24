package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.request.CreateCheckoutSessionRequest;
import com.ying.learneyjourney.dto.response.CheckoutSessionResponse;
import com.ying.learneyjourney.dto.response.WalletResponse;
import com.ying.learneyjourney.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/wallet")
    public WalletResponse getWallet(@RequestParam String userEmail) {
        return billingService.getWallet(userEmail);
    }

    @PostMapping("/checkout-session")
    public CheckoutSessionResponse createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request
    ) {
        return billingService.createCheckoutSession(request);
    }
}