package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.request.CreateCheckoutSessionRequest;
import com.ying.learneyjourney.dto.response.CheckoutSessionResponse;
import com.ying.learneyjourney.dto.response.WalletResponse;
import com.ying.learneyjourney.service.worksheet.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;
    private final FirebaseAuthUtil firebaseAuthUtil;

    @GetMapping("/wallet")
    public WalletResponse getWallet(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return billingService.getWallet(userId);
    }

    @PostMapping("/checkout-session")
    public CheckoutSessionResponse createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return billingService.createCheckoutSession(request, userId);
    }

}